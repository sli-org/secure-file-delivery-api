package za.co.api.statement.test.base;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.common.security.service.ClaimsService;

@ExtendWith(MockitoExtension.class)
public abstract class BaseServiceTest {

    @Mock
    protected ClaimsService claimsService;

    @BeforeEach
    protected void setUp() {
        // Subclasses override for additional setup
    }
}
