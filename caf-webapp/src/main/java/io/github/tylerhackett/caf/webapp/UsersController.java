package io.github.tylerhackett.caf.webapp;

import io.github.tylerhackett.caf.service.IUserService;
import io.github.tylerhackett.caf.service.dto.UserDto;
import io.github.tylerhackett.caf.service.dto.UserDtoFactory;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.logging.Logger;

import java.util.List;

@RequestScoped
@Transactional
@Path("/web/users")
public class UsersController {

    private final IUserService userService;

    private final Logger logger;

    @Context
    UriInfo uriInfo;

    public UsersController(IUserService userService, Logger logger) {
        this.userService = userService;
        this.logger = logger;
    }


    @CheckedTemplate
    public static class Templates {

        public static native TemplateInstance users(List<UserDto> users);

    }

    @PostConstruct
    public void init() {
        logger.info("Users Controller has been initialized!");
    }

    /*
     * Field names for the form for adding a user.
     */
    private static final String FIRST_NAME = "first-name";

    private static final String LAST_NAME = "last-name";

    private static final String EMAIL = "email";

    private static final String PASSWORD = "password";

    private static final String USERNAME = "username";

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    // Restrict to admin role
    @jakarta.annotation.security.RolesAllowed(io.github.tylerhackett.caf.service.Role.ADMIN)
    public TemplateInstance addUser(@FormParam(USERNAME) String username,
                                    @FormParam(PASSWORD) String password,
                                    @FormParam(FIRST_NAME) String firstName,
                                    @FormParam(LAST_NAME) String lastName,
                                    @FormParam(EMAIL) String email) {

        logger.info("Adding user: " + username);
        try {
            UserDto user = new UserDtoFactory().createUserDto();
            user.setUsername(username);
            user.setPassword(password.toCharArray());
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            userService.addUser(user);
            return Templates.users(userService.getUsers());
        } catch (IUserService.UserException e) {
            logger.errorf(e, "Failed to add user %s", username);
            throw new ServerErrorException("Failed to add user", 500);
        }
    }

    @GET
    // Restrict to admin and user roles
    @jakarta.annotation.security.RolesAllowed({io.github.tylerhackett.caf.service.Role.ADMIN, io.github.tylerhackett.caf.service.Role.USER})
    public TemplateInstance listUsers() {
        logger.info("Getting list of users.");
        return Templates.users(userService.getUsers());
    }

}