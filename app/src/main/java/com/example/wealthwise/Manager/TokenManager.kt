package com.example.wealthwise.Manager

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.wealthwise.ApiService
import com.example.wealthwise.DataClass.RefreshToken
import com.example.wealthwise.DataClass.TokenResponse
import com.example.wealthwise.R

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

@RequiresApi(Build.VERSION_CODES.M)
class TokenManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("Cookies", Context.MODE_PRIVATE)
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val BASE_URL = "https://10.0.2.2:8443"

    private fun getKey() : SecretKey {
        val existingKey = keyStore.getEntry("secret",null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey?: createKey()
    }


    private fun createKey() : SecretKey {
        return KeyGenerator.getInstance(ALGORITHM).apply {
            init(KeyGenParameterSpec.Builder("secret", KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(BLOCK_MODE)
                .setEncryptionPaddings(PADDING)
                .setUserAuthenticationRequired(false)
                .setRandomizedEncryptionRequired(true)
                .build())
        }.generateKey()
    }


    fun encrypt(token:String) : String?{
       return try{
           val cipher = Cipher.getInstance(TRANSFORMATION)
           cipher.init(Cipher.ENCRYPT_MODE, getKey())

           val cipherText = Base64.encodeToString(cipher.doFinal(token.toByteArray(Charsets.UTF_8)), Base64.DEFAULT)
           val iv = Base64.encodeToString(cipher.iv, Base64.DEFAULT)

           "${cipherText}.$iv"
       }catch (e: Exception){
           null
       }
    }

    fun decrypt(token: String) : String? {
        val array = token.split(".")
        val cipherData = Base64.decode(array[0], Base64.DEFAULT)
        val iv = Base64.decode(array[1], Base64.DEFAULT)

        return try{
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, getKey(), spec)

            val clearText = cipher.doFinal(cipherData)

            String(clearText,StandardCharsets.UTF_8)
        }catch (e: Exception){
            Log.e("decrypt", e.toString())
            null
        }
    }

    fun saveToken(tokenAccess: String, tokenRefresh: String) {
        val editor = sharedPreferences.edit()
        editor.putString("tokenAccess", encrypt(tokenAccess))
        editor.putString("tokenRefresh", encrypt(tokenRefresh))
        editor.apply()
    }

    fun getTokenAccess(): String? {
        val tokenAccess = sharedPreferences.getString("tokenAccess", null)
        return decrypt(tokenAccess.toString())
    }

    fun getTokenRefresh(): String? {
        val tokenRefresh = sharedPreferences.getString("tokenRefresh", null)
        return decrypt(tokenRefresh.toString())
    }

    private fun getTokenExpireTime(tokenAccess: String): Long {
        val tokenParts = tokenAccess.split(".")
        if (tokenParts.size >= 2) {
            try {
                val payload = tokenParts[1]
                val decodedPayload = Base64.decode(payload, Base64.URL_SAFE)
                val payloadJson = String(decodedPayload)
                val payloadObj = JSONObject(payloadJson)

                // Pobierz czas wygaśnięcia tokenu z pola "exp" (expiration time)
                if (payloadObj.has("exp")) {
                    return payloadObj.getLong("exp")
                }
            } catch (e: Exception) {
                // Obsługa błędu parsowania lub braku pola "exp"
                e.printStackTrace()
            }
        }
        return 0 // Domyślnie brak lub błąd w czasie wygaśnięcia
    }

    fun refreshToken(tokenRefresh: String ,resources: Resources): Pair<String?, String?> {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY

        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)

        val certificateFactory = CertificateFactory.getInstance("X.509")
        val certificateInputStream1 = resources.openRawResource(R.raw.ca)
        val yourTrustedCertificate1 = certificateFactory.generateCertificate(certificateInputStream1) as X509Certificate
        certificateInputStream1.close()


        keyStore.setCertificateEntry("ca", yourTrustedCertificate1)
        trustManagerFactory.init(keyStore)
        val trustManagers = trustManagerFactory.trustManagers

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustManagers, SecureRandom())

            val client = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            val apiService = retrofit.create(ApiService::class.java)
            val refreshToken = RefreshToken(tokenRefresh)
            val call = apiService.refreshToken(refreshToken)
            var newTokenAccess : String? = null
            var newTokenRefresh : String? = null

            call.enqueue(object : Callback<TokenResponse> {
                override fun onResponse(
                    call: Call<TokenResponse>,
                    response: Response<TokenResponse>
                ) {
                    if (response.isSuccessful) {
                        // Pomyślna odpowiedź

                        newTokenAccess = response.body()?.tokenAccess
                        newTokenRefresh = response.body()?.tokenRefresh
                    }
                }
                    override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                    }
            })

        return Pair(newTokenAccess, newTokenRefresh)
    }

     fun refreshTokenIfNeeded(resources: Resources): Boolean {
            val tokenAccess = getTokenAccess()
            val tokenRefresh = getTokenRefresh()

            if (tokenAccess == null || tokenRefresh == null) {
                // Brak dostępu do tokenów
                return false
            }

            val tokenExpireTime = getTokenExpireTime(tokenAccess)
            val currentTime = System.currentTimeMillis() / 1000

            if (tokenExpireTime <= currentTime) {
                // Token dostępu wygasł lub wygaśnie wkrótce, odświeżamy
                val (newTokenAccess, newTokenRefresh) = refreshToken(tokenRefresh, resources)

                if (newTokenAccess != null && newTokenRefresh != null) {
                    saveToken(newTokenAccess, newTokenRefresh)
                    return true
                }
            }

            return false
    }

    fun clearToken() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }


    companion object{

        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    }
}