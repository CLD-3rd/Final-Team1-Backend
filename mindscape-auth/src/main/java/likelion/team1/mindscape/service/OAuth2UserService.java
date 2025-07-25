package likelion.team1.mindscape.service;

import likelion.team1.mindscape.entity.User;
import likelion.team1.mindscape.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // 구글 로그인에서는 accountId = email로 취급.
        User user = userRepository.findByAccountId(email);

        if(user == null){
            user = User.builder()
                    .accountId(email)
                    .username(name)
                    .provider(provider)
                    .providerId(providerId)
                    .build();

            userRepository.save(user);
        }


        return new PrincipalDetails(user, oAuth2User.getAttributes());
    }
}
