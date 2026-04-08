package org.zzt.note.commons.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.athena.framework.security.api.model.UserContext;
import org.athena.framework.security.auth.core.filter.RequireTokenSecurityRequestInterceptor;
import org.springframework.http.HttpMethod;

import java.io.IOException;

/**
 * 放行 CORS 预检请求，其余请求保持 token 强制校验。
 */
public class OptionsAwareRequireTokenInterceptor extends RequireTokenSecurityRequestInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             String token,
                             UserContext userContext,
                             boolean ignored) throws IOException {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        return super.preHandle(request, response, token, userContext, ignored);
    }
}
