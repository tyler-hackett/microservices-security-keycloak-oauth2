package io.github.tylerhackett.caf.service;

import io.github.tylerhackett.caf.service.dto.UserDto;
import io.github.tylerhackett.caf.service.dto.UserDtoFactory;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@RequestScoped
public class UserService implements IUserService {

    private static final String CLIENT_REALM_KEY = "caf.client.realm";

    private static final String CLIENT_ID_KEY = "caf.client.client-id";

    private static final String CLIENT_ROLE_KEY = "caf.client.user-role";

    private static final UserDtoFactory USER_DTO_FACTORY = new UserDtoFactory();

    private final Logger logger;

    private final Keycloak keycloak;

    private UsersResource usersResource;

    private String clientUuid;

    private RoleResource clientRoleResource;

    @Inject
    @ConfigProperty(name = CLIENT_REALM_KEY)
    String clientRealm;

    // Inject config property for client webapp realm
    @Inject
    @ConfigProperty(name = CLIENT_ID_KEY)
    String clientId;

    // Inject config property for client role assigned to new user
    @Inject
    @ConfigProperty(name = CLIENT_ROLE_KEY)
    String clientRole;

    public UserService(Keycloak keycloak, Logger logger) {
        this.keycloak = keycloak;
        this.logger = logger;
    }

    @PostConstruct
    public void init() {
        usersResource = keycloak.realm(clientRealm).users();
        clientUuid = keycloak.realm(clientRealm).clients().findByClientId(clientId).getFirst().getId();
        clientRoleResource = keycloak.realm(clientRealm).clients().get(clientUuid).roles().get(clientRole);
    }

    @Override
    public void addUser(UserDto userDto) throws UserException {
        logger.info("Adding user " + userDto.getUsername());
        UserRepresentation user = new UserRepresentation();
        // Fill in user fields
        user.setUsername(userDto.getUsername());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setEnabled(true);
        user.setEmailVerified(true);
        org.keycloak.representations.idm.CredentialRepresentation credential =
                new org.keycloak.representations.idm.CredentialRepresentation();
        credential.setType(org.keycloak.representations.idm.CredentialRepresentation.PASSWORD);
        credential.setValue(new String(userDto.getPassword()));
        credential.setTemporary(false);
        user.setCredentials(List.of(credential));

        try (Response response = usersResource.create(user)) {
            if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                logger.error("Failed to create user " + userDto.getUsername());
                throw new UserException("Response from keycloak is " + response.getStatus());
            }
            /*
             * Get the user id from the Location header in the HTTP response
             */
            URI userUri = response.getLocation();
            String[] path = userUri.getPath().split("/");
            String userId = path[path.length - 1];
            /*
             * Add the newly created user to the role mapping.
             */
            keycloak.realm(clientRealm)
                    .users()
                    .get(userId)
                    .roles()
                    .clientLevel(clientUuid)
                    .add(List.of(clientRoleResource.toRepresentation()));
        }
    }

    @Override
    public List<UserDto> getUsers() {
        logger.info(String.format("Getting all users for realm %s and client %s", clientRealm, clientUuid));
        List<UserDto> userDtos = new ArrayList<UserDto>();
        // Return list of DTOs for users with role "user"
        for (UserRepresentation user : clientRoleResource.getUserMembers()) {
            UserDto userDto = USER_DTO_FACTORY.createUserDto();
            userDto.setUsername(user.getUsername());
            userDto.setFirstName(user.getFirstName());
            userDto.setLastName(user.getLastName());
            userDto.setEmail(user.getEmail());
            userDtos.add(userDto);
        }
        return userDtos;
    }
}
