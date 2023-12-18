package ca.dtadmi.todolist.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Load shedding filter :
 * Monitors number of concurrent requests
 * Return an HTTP 503 error code if more than 50 at a time
 * */

@Component
public class InFlightRequestFilter implements Filter {

    private static final int MAX_NUMBER_OF_REQUESTS = 50;
    private final AtomicInteger numberRequests = new AtomicInteger(0);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if(numberRequests.get() >= MAX_NUMBER_OF_REQUESTS){
            HttpServletResponse resp = (HttpServletResponse) response;
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            byte[] message = "The server is experiencing an unusually large number of concurrent requests. Please try again later.".getBytes();
            resp.getOutputStream().write(message);
            return;
        }
        numberRequests.incrementAndGet();

        try {
            chain.doFilter(request, response);
        } finally {
            numberRequests.decrementAndGet();
        }
    }
}
