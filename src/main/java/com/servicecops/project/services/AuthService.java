package com.servicecops.project.services;

import com.jet.moonlight.annotations.Action;
import com.jet.moonlight.annotations.Doc;
import com.jet.moonlight.annotations.Field;
import com.jet.moonlight.annotations.FieldFormat;
import com.jet.moonlight.annotations.FieldType;
import com.jet.moonlight.annotations.JetFields;
import com.jet.moonlight.annotations.MarkedAsJetService;
import com.jet.moonlight.annotations.PostOnly;
import com.jet.moonlight.services.JetRequest;
import com.jet.moonlight.services.JetResponse;
import com.servicecops.project.config.ApplicationConf;
import com.servicecops.project.config.JwtUtility;
import com.servicecops.project.models.database.SystemUserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.HashMap;
import java.util.Map;


@RequiredArgsConstructor
@Doc(
        summary = "Authentication",
        description = """
                Session and identity actions.

                Classic RPC: `POST /api/v1` with `{ "service": "auth", "action": "…" }`.
                Path form: `POST /api/v1/auth/{action}`.
                """,
        tags = {"Authentication"},
        hidden = false
)
@MarkedAsJetService(name = "auth", description = "Authentication — login and identity")
public class AuthService extends UniversalService {
    private final AuthenticationManager authenticationManager;
    private final ApplicationConf userDetailService;
    private final JwtUtility jwtUtility;

    @Doc(
            summary = "Log in",
            description = """
                    Authenticate with **username** and **password**. Public — no Bearer token.

                    - Success: JWT in `returnData.token` plus the user profile
                    - Failure: non-zero `returnCode` (typically 401)

                    Try-it: `POST /api/v1/auth/login`. RPC: `POST /api/v1` with
                    `{ "service": "auth", "action": "login", "username", "password" }`.
                    """,
            tags = {"Authentication"},
            hidden = false,
            responseDescription = "JWT and authenticated user profile",
            responseExample = """
                    {"token":"eyJhbGciOiJIUzI1NiJ9...","user":{"username":"admin","lastName":"Admin"}}
                    """
    )
    @JetFields({
            @Field(
                    name = "username",
                    required = true,
                    type = FieldType.STRING,
                    format = FieldFormat.NONE,
                    pattern = "",
                    minLength = 1,
                    maxLength = 128,
                    min = "",
                    max = "",
                    in = "",
                    description = "Account login (username or email)",
                    example = "admin"
            ),
            @Field(
                    name = "password",
                    required = true,
                    type = FieldType.STRING,
                    format = FieldFormat.NONE,
                    pattern = "",
                    minLength = 1,
                    maxLength = 128,
                    min = "",
                    max = "",
                    in = "",
                    description = "Account password",
                    example = "changeme"
            )
    })
    @PostOnly
    @Action(name = "login")
    public JetResponse login(JetRequest request){
        String username= request.getString("username");
        String password= request.getString("password");

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        final SystemUserModel userDetails = userDetailService.loadUserByUsername(username);
        final String token = jwtUtility.generateToken(userDetails);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token); // this is the jwt token the user can user from now on.
        response.put("user", userDetails);

        return JetResponse.ok(String.format("Welcome back, %s", userDetails.getLastName())).withData(response);
    }
}
