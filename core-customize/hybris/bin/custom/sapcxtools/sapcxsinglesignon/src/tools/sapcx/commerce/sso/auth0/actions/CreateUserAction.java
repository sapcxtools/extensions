package tools.sapcx.commerce.sso.auth0.actions;

import java.security.SecureRandom;
import java.util.Map;

import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.users.User;

import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CreateUserAction implements SdkAction<User> {
	private static final Logger LOG = LoggerFactory.getLogger(CreateUserAction.class);

	private SecureRandom randomPasswordGenerator = new SecureRandom();

	static User createUser(CustomerModel customer) throws Auth0Exception {
		return new CreateUserAction().execute(Map.of("customer", customer));
	}

	private CreateUserAction() {
		// Avoid instantiation
	}

	@Override
	public User execute(Map<String, Object> parameter) throws Auth0Exception {
		CustomerModel customer = getWithType(parameter, "customer", CustomerModel.class);
		String customerId = customer.getUid();

		User user = null;
		try {
			Converter<CustomerModel, User> customerConverter = getCustomerConverter();
			User userInfo = customerConverter.convert(customer);

			// Add one time information for creation process
			userInfo.setConnection(getCustomerConnection());
			userInfo.setPassword(getRandomPassword());

			return user = fetch(managementAPI().users().create(userInfo));
		} catch (Auth0Exception exception) {
			LOG.debug(String.format("Create user with ID '%s' failed!", customerId), exception);
			throw exception;
		} finally {
			LOG.debug("Create user with ID '{}' resulted in: '{}'.", customerId, user != null ? user.getId() : "-error-");
		}
	}

	private char[] getRandomPassword() {
		byte[] password = new byte[32];
		randomPasswordGenerator.nextBytes(password);
		return new String(password).toCharArray();
	}
}
