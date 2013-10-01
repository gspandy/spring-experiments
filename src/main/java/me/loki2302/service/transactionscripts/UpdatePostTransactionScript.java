package me.loki2302.service.transactionscripts;

import me.loki2302.dto.BlogServiceErrorCode;
import me.loki2302.dto.PostDTO;
import me.loki2302.entities.Post;
import me.loki2302.entities.User;
import me.loki2302.repositories.PostRepository;
import me.loki2302.service.implementation.AuthenticationManager;
import me.loki2302.service.implementation.BlogServiceException;
import me.loki2302.service.implementation.PostMapper;
import me.loki2302.service.validation.ThrowingValidator;
import me.loki2302.service.validation.subjects.PostSubject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class UpdatePostTransactionScript {
	@Autowired ThrowingValidator throwingValidator;
	@Autowired AuthenticationManager authenticationManager;	
	@Autowired PostRepository postRepository;	
	@Autowired PostMapper postMapper;
	
	public PostDTO updatePost(
			String sessionToken, 
			long postId, 
			String text) throws BlogServiceException {
		
		PostSubject postSubject = new PostSubject();
		postSubject.text = text;
		throwingValidator.Validate(postSubject);
		
		User user = authenticationManager.getUser(sessionToken);
		Post post = postRepository.findOne(postId);
		if(post == null) {
			throw new BlogServiceException(BlogServiceErrorCode.NoSuchPost);
		}
		
		if(!post.getAuthor().equals(user)) {
			throw new BlogServiceException(BlogServiceErrorCode.NoPermissionsToAccessPost);
		}
		
		post.setText(text);
		post = postRepository.save(post);
		
		return postMapper.build(post);
	}	
}