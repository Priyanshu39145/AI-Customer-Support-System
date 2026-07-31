package com.Spring.AI_Customer_Support_Backend_System.Services;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
//It will be a filter which will intercept the request ---- Just like JWTAuthFilter
//OncePerRequestFilter guarantees that the filter runs only once per request. We dont want to accidentally check the same request multiple time

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitingService rateLimitingService;

    // Skip CORS preflight OPTIONS requests to allow them through without rate limiting
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getServletPath();

        // Only protect these APIs
        return !(path.startsWith("/auth/login")
                || path.startsWith("/auth/register")
                || path.startsWith("/chat")
                || path.startsWith("/conversation")
                || path.startsWith("/tickets"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //We have to extract the client's IP so that we can pass it as key ---
        //String clientIp = request.getRemoteAddr(); //It can give us the IP ---
        //But the problem with the above method is that ---
        //In production if we use nginx or AWS load balancer or cloudflare --- The code will see the loadBalancer's IP for every request
        //As a result we accidentally rate limit our own load balancer blocking all our traffic to our application.

        String clientIp = getClientIp(request);
        //We get the bucket where the key is the clientIp and the requests the clientIp has remaining --
        log.debug("Rate limit check for IP: {} on endpoint: {}", clientIp, request.getRequestURI());
        Bucket tokenBucket = rateLimitingService.resolveBucket(clientIp);

        //This call attempts to consume one token and also tells us how many tokens are left or how long the client has to wait if the bucket is empty
        //We consume one token to say that we have consumed one request in our quota and then it returns the remaining requests
        var probe = tokenBucket.tryConsumeAndReturnRemaining(1);

        //If the probe had requests remaining and it was consumed by 1 --- then we send it to the controllers by doFilter
        //It means that I had consumed my one valid request and reaching the controllers now
        if(probe.isConsumed())  {
            //request is allowed then
            log.debug("Request allowed for IP: {} | Remaining tokens: {}", clientIp, probe.getRemainingTokens());
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request,response);
        }
        //client has exhausted the request quota ---- probe was not consumed as we have no requests left
        else    {
            //We first calculate the refill time --- by getting the Nanosecs and dividing it to make seconds
            var waitForRefill = probe.getNanosToWaitForRefill()/1_000_000_000;
            //We set the response status
            log.warn("Rate limit exceeded for IP: {} | Retry after: {} seconds", clientIp, waitForRefill);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            //We add the header to the response
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            //We set the content type as json
            response.setContentType("application/json");
            //We write the jsonResponse %s is the placeholder which is filled by formatted method ----
            String jsonResponse = """
                     {
                     "status" : %s,
                     "error": "Too Many Requests",
                     "message" : "You have exhausted your API request Quota",
                     "retryAfterSeconds" : %s
                     }
                     """.formatted(HttpStatus.TOO_MANY_REQUESTS.value(),waitForRefill);
            response.getWriter().write(jsonResponse);
        }
    }

    private String getClientIp(HttpServletRequest request)  {
        //We will check the X-Forwarded-For header to get the real userId behind the proxy
        //The header is commonly added by proxies or loadBalancers
        String xHeader = request.getHeader("X-Forwarded-For");
        //If null then no proxy is there
        if(xHeader==null || xHeader.isEmpty())
            return request.getRemoteAddr();

        //normally header consists of multiple IPs which are comma separated --- and the first IP is the clientIp
        return xHeader.split(",")[0].trim();
    }
}
//Done
