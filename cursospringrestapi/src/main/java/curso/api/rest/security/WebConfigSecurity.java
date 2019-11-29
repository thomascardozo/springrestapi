package curso.api.rest.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import curso.api.rest.service.ImplementacaoUserDetailsService;

/*Mapeia URLs, endereços, autoriza ou bloqueia acessos a URL*/
@Configuration
@EnableWebSecurity
public class WebConfigSecurity extends WebSecurityConfigurerAdapter{
	
	@Autowired
	private ImplementacaoUserDetailsService implementacaoUserDetailsService; 
	
	/* Configura solicitaçoes de acesso por HTTP*/
	@Override
		protected void configure(HttpSecurity http) throws Exception {
			
		/*Ativando a proteção contra usuarios que não estão validados por token*/
		http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
		
		/*Ativando a permissão geral para acesso a pagina inicial do sistema. Ex: sistema.com.br/ ou sistema.com.br/index.html */
		.disable().authorizeRequests().antMatchers("/").permitAll()
		.antMatchers("/index").permitAll()
		
		/*URL de logout - redireciona após usuário deslogar do sistema para a página index*/
		.anyRequest().authenticated().and().logout().logoutSuccessUrl("/index")
		
		/* Mapeia URL de Logout e invalida o usuario*/
		.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
		
		/*Filtra requisições de login para autenticação */
		.and().addFilterBefore(new JWTLoginFilter("/login", authenticationManager()), UsernamePasswordAuthenticationFilter.class)
		
		
		/* Filtra as demais requisições para verificar a presença do token JWT no HEADER HTTP*/
		.addFilterBefore(new JWTApiAutenticacaoFilter(), UsernamePasswordAuthenticationFilter.class);		
		}
	
	@Override
		protected void configure(AuthenticationManagerBuilder auth) throws Exception {
			
		/* Service que irá consultar o usuario no banco de dados */	
		auth.userDetailsService(implementacaoUserDetailsService)
		
		/* Padrão de codificação de senha do nosso usuario*/
		.passwordEncoder(new BCryptPasswordEncoder());
		}

}
