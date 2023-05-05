package com.example;

import java.sql.Types;
import java.util.List;

import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UsersController {

	private final JdbcTemplate jdbc;
	private final RowMapper<User> rowMapper = DataClassRowMapper.newInstance(User.class);

	public UsersController(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	@GetMapping
	@Transactional(readOnly = true)
	public Object getAll() {
		return jdbc.query("""
				select id, username
				from users
				order by id asc
				""", rowMapper);
	}

	@GetMapping("/{id}")
	@Transactional(readOnly = true)
	public Object get(@PathVariable Integer id) {
		return jdbc.queryForObject("""
				select id, username
				from users
				where id = ?
				""", rowMapper, id);
	}

	@PostMapping
	@Transactional
	public Object create(@RequestParam String username) {
		PreparedStatementCreatorFactory pscf = new PreparedStatementCreatorFactory("""
				insert into users (username) values (?)
				""", Types.VARCHAR);
		pscf.setReturnGeneratedKeys(true);
		pscf.setGeneratedKeysColumnNames("id");
		PreparedStatementCreator psc = pscf.newPreparedStatementCreator(List.of(username));
		KeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		jdbc.update(psc, generatedKeyHolder);
		Integer id = generatedKeyHolder.getKeyAs(Integer.class);
		return new User(id, username);
	}
}
