package com.mit.outpass.service;

import com.mit.outpass.entity.Security;
import com.mit.outpass.exception.ResourceNotFoundException;
import com.mit.outpass.repository.SecurityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SecurityService {
    
    @Autowired
    private SecurityRepository securityRepository;
    
    public Security getSecurityById(Long id) {
        return securityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Security", "id", id));
    }
    
    public Security getSecurityBySecurityId(String securityId) {
        return securityRepository.findBySecurityId(securityId)
                .orElseThrow(() -> new ResourceNotFoundException("Security", "securityId", securityId));
    }
    
    public Security getSecurityByUsername(String username) {
        return securityRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Security", "username", username));
    }
    
    public List<Security> getAllSecurity() {
        return securityRepository.findAll();
    }
    
    public List<Security> getSecurityByShift(String shift) {
        return securityRepository.findByShift(shift);
    }
    
    public List<Security> getSecurityByGate(String gateAssigned) {
        return securityRepository.findByGateAssigned(gateAssigned);
    }
    
    public Security updateSecurity(Security security) {
        return securityRepository.save(security);
    }
}