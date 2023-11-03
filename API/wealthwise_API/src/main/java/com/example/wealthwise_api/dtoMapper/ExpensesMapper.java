package com.example.wealthwise_api.dtoMapper;

import com.example.wealthwise_api.DTO.ExpensesResponse;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Component;


@Component
public class ExpensesMapper {

    public ExpensesResponse mapToExpensesResponse(Tuple tuple) {
        String category = tuple.get("category", String.class);
        Double value = tuple.get("value", Double.class);

        return new ExpensesResponse(category, value);
    }
}