package deso.future_bot.web.rest

import deso.future_bot.model.dto.UserDto
import deso.future_bot.model.rest.AddUser
import deso.future_bot.security.SecurityUtils
import deso.future_bot.service.UserService
import deso.future_bot.web.rest.UserResource
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.net.URISyntaxException
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/api")
class UserResource(private val userService: UserService) {
    private val log = LoggerFactory.getLogger(UserResource::class.java)

    @PostMapping("/users/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Throws(URISyntaxException::class)
    fun create(@RequestBody user: @Valid AddUser): ResponseEntity<UserDto> {
        return ResponseEntity
            .created(URI("/api/register/"))
            .body(userService.createUser(user))
    }

    @GetMapping("/users")
    fun getAllPublicUsers(pageable: Pageable): ResponseEntity<List<UserDto>> {
        log.debug("REST request to get all public User names")
        if (!onlyContainsAllowedProperties(pageable) && "deso" != SecurityUtils.getCurrentUser().username) {
            return ResponseEntity.badRequest().build()
        }
        val page = userService.getUsers(pageable)
        return ResponseEntity(page.content, HttpStatus.OK)
    }

    private fun onlyContainsAllowedProperties(pageable: Pageable): Boolean {
        return pageable.sort.all { sort -> ALLOWED_ORDERED_PROPERTIES.contains(sort.property) }
    }

    companion object {
        private val ALLOWED_ORDERED_PROPERTIES =
            mutableListOf("id", "login", "firstName", "lastName", "email", "activated", "langKey")
    }
}