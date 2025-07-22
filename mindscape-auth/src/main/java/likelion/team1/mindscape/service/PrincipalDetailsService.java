package likelion.team1.mindscape.service;

import likelion.team1.mindscape.entity.User;
import likelion.team1.mindscape.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // 로그인 시도할 때 스프링 시큐리티가 자동으로 호출하는 메소드
    // username으로 DB에서 사용자를 조회하여 PrincipalDetails 객체로 변환
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        return new PrincipalDetails(user);
    }

}
