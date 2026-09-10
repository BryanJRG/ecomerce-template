package com.bjdev.ecomercebase.services.interfaces;

import com.bjdev.ecomercebase.dto.request.CheckoutRequest;
import com.bjdev.ecomercebase.dto.response.CheckoutResult;

public interface CheckoutService {

    CheckoutResult checkout(Long clientId, CheckoutRequest request);
}
