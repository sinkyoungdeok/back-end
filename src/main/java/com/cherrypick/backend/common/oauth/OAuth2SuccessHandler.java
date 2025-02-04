package com.cherrypick.backend.common.oauth;

import com.cherrypick.backend.common.config.AppProperties;
import com.cherrypick.backend.common.exception.ErrorCode;
import com.cherrypick.backend.common.exception.UnAuthorizedException;
import com.cherrypick.backend.common.jwt.TokenProvider;
import com.cherrypick.backend.common.util.CookieUtils;
import com.cherrypick.backend.domain.user.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

  private final TokenProvider jwtTokenUtil;

  private final AppProperties appProperties;

  private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

  //oauth2인증이 성공적으로 이뤄졌을 때 실행된다
  //token을 포함한 uri을 생성 후 인증요청 쿠키를 비워주고 redirect 한다.
  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
    Authentication authentication) throws IOException {
    String targetUrl = determineTargetUrl(request, response, authentication);
    if (response.isCommitted()) {
      logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
      return;
    }
    clearAuthenticationAttributes(request, response);
    getRedirectStrategy().sendRedirect(request, response, targetUrl);
  }

  //token을 생성하고 이를 포함한 프론트엔드로의 uri를 생성한다.
  protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
    Authentication authentication) {
    Optional<String> redirectUri = CookieUtils.getCookie(request,
        HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)
      .map(Cookie::getValue);
    if (redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
      throw new UnAuthorizedException(
        "Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication",
        ErrorCode.UNAUTHORIZED);
    }
    String targetUrl = redirectUri.orElse(getDefaultTargetUrl());
    String token = jwtTokenUtil.createToken(authentication);
    return UriComponentsBuilder.fromUriString(targetUrl)
      .queryParam("token", token)
      .build().toUriString();
  }

  //인증정보 요청 내역을 쿠키에서 삭제한다.
  protected void clearAuthenticationAttributes(HttpServletRequest request,
    HttpServletResponse response) {
    super.clearAuthenticationAttributes(request);
    httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request,
      response);
  }

  //application.properties에 등록해놓은 Redirect uri가 맞는지 확인한다. (app.redirect-uris)
  private boolean isAuthorizedRedirectUri(String uri) {
    URI clientRedirectUri = URI.create(uri);
    return appProperties.getOauth2().getAuthorizedRedirectUris()
      .stream()
      .anyMatch(authorizedRedirectUri -> {
        // Only validate host and port. Let the clients use different paths if they want to
        URI authorizedURI = URI.create(authorizedRedirectUri);
        return authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
          && authorizedURI.getPort() == clientRedirectUri.getPort();
      });
  }
}