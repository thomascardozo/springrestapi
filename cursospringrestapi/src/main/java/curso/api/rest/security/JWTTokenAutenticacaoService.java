package curso.api.rest.security;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import curso.api.rest.ApplicationContextLoad;
import curso.api.rest.model.Usuario;
import curso.api.rest.repositoy.UsuarioRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;


@Service
@Component
public class JWTTokenAutenticacaoService {
	
	/* tEMPO DE expiração do token, em milisegundos - neste caso 2 dias*/
	private static final long EXPIRATION_TIME = 172800000;
	
	/*senha unica para compor a autenticacao e ajudar na segurança*/	
	private static final String SECRET = "SenhaSuperSecreta";
	
	/*Prefixo padrão de Token*/
	private static final String TOKEN_PREFIX = "Bearer";
	
	
	private static final String HEADER_STRING = "Authorization";
	
	/* Gerando token de autenticação e adicionando o cabeçaho e resposta HTTP que voltará ao navegador*/
	public void addAuthentication(HttpServletResponse response, String username)
	throws IOException{
		
		/* Montagem do token*/
		
		String JWT = Jwts.builder() /*cHAMA o gerador de token */
				.setSubject(username)/*  adiciona o usuario*/
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))/* tempo de expiração*/
				.signWith(SignatureAlgorithm.HS512, SECRET).compact();/*Compactação e algoritmo de geração de senha*/
		
		/* Junta o token com o prefixo*/	
		String token = TOKEN_PREFIX + " " + JWT /* Bearer uqquwheeueuejejejeu*/;
			
		/* Adiciona o cabeçaho http*/
		response.addHeader(HEADER_STRING, token); /* Authorization: Bearer uqquwheeueuejejejeu */
		
		
		/* Escreve token como resposta no corpo do http  */		
		response.getWriter().write("{\"Authorization\": \""+token+"\"}");	
	}
	
	/* Retorna o usuario validado com token ou caso não seja válido retorna null*/
	
	public Authentication getAuthentication(HttpServletRequest request) {
		
		/* Pega o token enviado no cabeçalhp HTTP*/
		
		String token = request.getHeader(HEADER_STRING);
		
		if(token != null) {
			
			/* faz a validação do token do usuário na requisição*/
			String user = Jwts.parser().setSigningKey(SECRET) /* viria TOKEN_PREFIX completo: Bearer uqquwheeueuejejejeu */
					.parseClaimsJws(token.replace(TOKEN_PREFIX, "")) /* retornaria sem a String, somente assim: uqquwheeueuejejejeu */ 
					.getBody().getSubject(); /* Retornaria somente o usuario Joao SIlva, por exemplo*/
			if( user!= null) {
				
				Usuario usuario =  ApplicationContextLoad.getApplicationContext()
						.getBean(UsuarioRepository.class)
						.findUserByLogin(user);
				
				/* Retornando o usuario logado*/
				if(usuario != null) {
					
					return new UsernamePasswordAuthenticationToken(
							usuario.getLogin(), 
							usuario.getSenha(), 
							usuario.getAuthorities());					
					} 
				}
			
			} 	return null;/* não autorizado*/
		}
}
