package com.cherrypick.backend.domain.user;

import com.cherrypick.backend.common.exception.ErrorCode;
import com.cherrypick.backend.common.exception.UnAuthorizedException;
import com.cherrypick.backend.common.jwt.TokenProvider;
import com.cherrypick.backend.domain.user.UserCommand.ReissueRequest;
import com.cherrypick.backend.domain.user.UserInfo.Token;
import com.cherrypick.backend.infrastructure.redis.RedisRepository;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final TokenProvider tokenProvider;
  private final AuthenticationManagerBuilder authenticationManagerBuilder;
  private final RedisRepository redisRepository;

  @Value("${jwt.refresh-token-validity-in-seconds}")
  private long refreshTokenValidityInMilliseconds;

  private final String ID_PREFIX = "RT:";

  @Override
  public UserInfo.Token authorize(UserCommand.UserLoginRequest command) {
    UsernamePasswordAuthenticationToken authenticationToken =
      new UsernamePasswordAuthenticationToken(command.getProviderId(), command.getPassword());

    Authentication authentication = authenticationManagerBuilder.getObject()
      .authenticate(authenticationToken);
    SecurityContextHolder.getContext().setAuthentication(authentication);

    Token token = tokenProvider.createTokens(authentication);
    redisRepository.setValue(
      ID_PREFIX + command.getProviderId(),
      token.getRefreshToken(),
      refreshTokenValidityInMilliseconds,
      TimeUnit.MILLISECONDS);

    return token;
  }

  @Override
  public UserInfo.Token reissue(ReissueRequest command) {
    if (!tokenProvider.validateToken(command.getRefreshToken())) {
      throw new UnAuthorizedException("유효하지 않은 RefreshToken 입니다.", ErrorCode.UNAUTHORIZED);
    }

    val loginId = tokenProvider.getUsernameFromToken(command.getRefreshToken());
    val redisRefreshToken = redisRepository.getValue(ID_PREFIX + loginId)
      .orElseThrow(
        () -> new UnAuthorizedException("로그인 시간이 만료되었습니다.", ErrorCode.UNAUTHORIZED));

    if (!redisRefreshToken.equals(command.getRefreshToken())) {
      throw new UnAuthorizedException("유효하지 않은 RefreshToken 입니다.", ErrorCode.UNAUTHORIZED);
    }

    Authentication authentication = tokenProvider.getAuthentication(command.getAccessToken());
    SecurityContextHolder.getContext().setAuthentication(authentication);

    Token token = tokenProvider.createTokens(authentication);
    redisRepository.setValue(
      ID_PREFIX + loginId,
      token.getRefreshToken(),
      refreshTokenValidityInMilliseconds,
      TimeUnit.MILLISECONDS);

    return token;
  }
}
