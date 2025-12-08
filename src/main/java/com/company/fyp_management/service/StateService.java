package com.company.fyp_management.service;

import com.company.fyp_management.entity.State;
import com.company.fyp_management.repository.StatesRepository;
import org.springframework.stereotype.Service;

@Service
public class StateService {

    private final StatesRepository statesRepository;

    public StateService(StatesRepository statesRepository) {
        this.statesRepository = statesRepository;
    }

    public State updateState(State state) {
        return statesRepository.save(state);
    }

    public State getStateByKey(String key) {
        return statesRepository.findById(key)
                .orElseThrow(() -> new RuntimeException("State not found with key " + key));
    }

    public State getStateByName(String name) {
        return statesRepository.findById(name).orElse(null);
    }
}

