package com.tugaspbo.backend;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CafeApiIntegrationTest {

	private static final AtomicInteger SEQ = new AtomicInteger();

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper objectMapper;

	// ---------- helpers ----------

	private String uniqueEmail() {
		return "user" + SEQ.incrementAndGet() + "-" + UUID.randomUUID().toString().substring(0, 6)
				+ "@example.com";
	}

	private String registerCustomer(String email) throws Exception {
		MvcResult result = mvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"Budi","email":"%s","password":"password123","phone":"08123456789"}
						""".formatted(email)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.role").value("CUSTOMER"))
				.andReturn();
		return read(result, "data.token");
	}

	private String adminToken() throws Exception {
		MvcResult result = mvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"admin@cafe.local\",\"password\":\"admin123\"}"))
				.andExpect(status().isOk())
				.andReturn();
		return read(result, "data.token");
	}

	private String read(MvcResult result, String jsonPointer) throws Exception {
		JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
		return node.at("/" + jsonPointer.replace('.', '/')).asText();
	}

	private String createOrder(String token, String body) throws Exception {
		MvcResult result = mvc.perform(post("/api/orders")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.status").value("PENDING"))
				.andReturn();
		return read(result, "data.orderId");
	}

	// ---------- auth ----------

	@Test
	void registerLoginAndPasswordRules() throws Exception {
		String email = uniqueEmail();
		String token = registerCustomer(email);
		org.junit.jupiter.api.Assertions.assertFalse(token.isBlank());

		mvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"%s\",\"password\":\"password123\"}".formatted(email)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.token").isNotEmpty())
				.andExpect(jsonPath("$.data.role").value("CUSTOMER"));

		// duplicate email -> 409
		mvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"Budi","email":"%s","password":"password123","phone":"0812"}
						""".formatted(email)))
				.andExpect(status().isConflict());

		// wrong password -> 401
		mvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"%s\",\"password\":\"wrong\"}".formatted(email)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void adminRegisterIsRestrictedToAdmins() throws Exception {
		String body = """
				{"name":"Boss","email":"%s","password":"admin123"}
				""".formatted(uniqueEmail());

		mvc.perform(post("/api/auth/admin/register")
				.contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isUnauthorized());

		String customerToken = registerCustomer(uniqueEmail());
		mvc.perform(post("/api/auth/admin/register")
				.header("Authorization", "Bearer " + customerToken)
				.contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isForbidden());

		mvc.perform(post("/api/auth/admin/register")
				.header("Authorization", "Bearer " + adminToken())
				.contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.role").value("ADMIN"));
	}

	// ---------- menu ----------

	@Test
	void menuReadIsPublicAndSeeded() throws Exception {
		mvc.perform(get("/api/menu"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(5))))
				.andExpect(jsonPath("$.data[?(@.id=='MENU-001')]").exists());

		mvc.perform(get("/api/menu/available")).andExpect(status().isOk());
		mvc.perform(get("/api/menu/MENU-001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.name").value("Nasi Goreng"));
		mvc.perform(get("/api/menu/category/FOOD"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))));
		mvc.perform(get("/api/menu/MISSING")).andExpect(status().isNotFound());
	}

	@Test
	void menuWriteRequiresAdmin() throws Exception {
		String body = """
				{"name":"Test Latte","price":15000,"category":"DRINK","available":true}
				""";

		mvc.perform(post("/api/menu").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isUnauthorized());

		String customerToken = registerCustomer(uniqueEmail());
		mvc.perform(post("/api/menu")
				.header("Authorization", "Bearer " + customerToken)
				.contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isForbidden());

		mvc.perform(post("/api/menu")
				.header("Authorization", "Bearer " + adminToken())
				.contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.id", startsWith("MENU-")));
	}

	@Test
	void adminCanUpdatePriceAndAvailability() throws Exception {
		String admin = adminToken();

		mvc.perform(patch("/api/menu/MENU-001/price")
				.header("Authorization", "Bearer " + admin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"price\":26000}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.price").value(26000));

		mvc.perform(patch("/api/menu/MENU-001/price")
				.header("Authorization", "Bearer " + admin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"price\":-1}"))
				.andExpect(status().isBadRequest());

		mvc.perform(patch("/api/menu/MENU-001/availability")
				.header("Authorization", "Bearer " + admin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"available\":false}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.available").value(false));

		// restore for other tests sharing the context
		mvc.perform(patch("/api/menu/MENU-001/availability")
				.header("Authorization", "Bearer " + admin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"available\":true}"));
	}

	// ---------- orders ----------

	@Test
	void createOrderValidations() throws Exception {
		String token = registerCustomer(uniqueEmail());

		mvc.perform(post("/api/orders")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"items\":[]}"))
				.andExpect(status().isBadRequest());

		mvc.perform(post("/api/orders")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"items\":[{\"menuItemId\":\"MENU-001\",\"quantity\":0}]}"))
				.andExpect(status().isBadRequest());

		mvc.perform(post("/api/orders")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"items\":[{\"menuItemId\":\"NOPE\",\"quantity\":1}]}"))
				.andExpect(status().isNotFound());

		mvc.perform(post("/api/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"items\":[{\"menuItemId\":\"MENU-001\",\"quantity\":1}]}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void orderWithMultipleItemsComputesTotal() throws Exception {
		String token = registerCustomer(uniqueEmail());

		MvcResult result = mvc.perform(post("/api/orders")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"items":[
							{"menuItemId":"MENU-003","quantity":2},
							{"menuItemId":"MENU-001","quantity":1}
						]}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.items", hasSize(2)))
				.andReturn();

		String orderId = read(result, "data.orderId");

		mvc.perform(get("/api/orders/" + orderId + "/total")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.total").value(61000));
	}

	@Test
	void customerCannotAccessOtherCustomersOrder() throws Exception {
		String owner = registerCustomer(uniqueEmail());
		String other = registerCustomer(uniqueEmail());
		String orderId = createOrder(owner,
				"{\"items\":[{\"menuItemId\":\"MENU-001\",\"quantity\":1}]}");

		mvc.perform(get("/api/orders/" + orderId)
				.header("Authorization", "Bearer " + other))
				.andExpect(status().isForbidden());

		mvc.perform(get("/api/orders/" + orderId)
				.header("Authorization", "Bearer " + adminToken()))
				.andExpect(status().isOk());
	}

	@Test
	void addAndRemoveItemsWhilePending() throws Exception {
		String token = registerCustomer(uniqueEmail());
		String orderId = createOrder(token,
				"{\"items\":[{\"menuItemId\":\"MENU-001\",\"quantity\":1}]}");

		mvc.perform(post("/api/orders/" + orderId + "/items")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"menuItemId\":\"MENU-005\",\"quantity\":2}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.items", hasSize(2)));

		mvc.perform(delete("/api/orders/" + orderId + "/items/MENU-005")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.items", hasSize(1)));
	}

	@Test
	void unavailableItemCannotBeOrdered() throws Exception {
		String admin = adminToken();
		String token = registerCustomer(uniqueEmail());

		mvc.perform(patch("/api/menu/MENU-002/availability")
				.header("Authorization", "Bearer " + admin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"available\":false}"))
				.andExpect(status().isOk());

		mvc.perform(post("/api/orders")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"items\":[{\"menuItemId\":\"MENU-002\",\"quantity\":1}]}"))
				.andExpect(status().isBadRequest());

		mvc.perform(patch("/api/menu/MENU-002/availability")
				.header("Authorization", "Bearer " + admin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"available\":true}"));
	}

	// ---------- payments + lifecycle ----------

	@Test
	void cashPaymentFlowEndToEnd() throws Exception {
		String token = registerCustomer(uniqueEmail());
		String admin = adminToken();
		String orderId = createOrder(token,
				"{\"items\":[{\"menuItemId\":\"MENU-003\",\"quantity\":2}]}");

		MvcResult order = mvc.perform(get("/api/orders/" + orderId)
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk()).andReturn();
		double total = objectMapper.readTree(order.getResponse().getContentAsString())
				.at("/data/total").asDouble();

		// insufficient cash -> 400, order stays PENDING
		mvc.perform(post("/api/orders/" + orderId + "/payments/cash")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"cashGiven\":1}"))
				.andExpect(status().isBadRequest());

		// sufficient cash -> PAID with change
		mvc.perform(post("/api/orders/" + orderId + "/payments/cash")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"cashGiven\":" + (total + 5000) + "}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.method").value("CASH"))
				.andExpect(jsonPath("$.data.status").value("SUCCESS"))
				.andExpect(jsonPath("$.data.change").value(5000));

		mvc.perform(get("/api/orders/" + orderId)
				.header("Authorization", "Bearer " + token))
				.andExpect(jsonPath("$.data.status").value("PAID"));

		// duplicate payment -> 409
		mvc.perform(post("/api/orders/" + orderId + "/payments/cash")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"cashGiven\":999999}"))
				.andExpect(status().isConflict());

		// paid order cannot be cancelled or modified
		mvc.perform(post("/api/orders/" + orderId + "/cancel")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isConflict());
		mvc.perform(post("/api/orders/" + orderId + "/items")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"menuItemId\":\"MENU-001\",\"quantity\":1}"))
				.andExpect(status().isConflict());

		// admin lifecycle: PAID -> PROCESSING -> COMPLETED
		mvc.perform(patch("/api/admin/orders/" + orderId + "/processing")
				.header("Authorization", "Bearer " + admin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("PROCESSING"));
		mvc.perform(patch("/api/admin/orders/" + orderId + "/complete")
				.header("Authorization", "Bearer " + admin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("COMPLETED"));
		mvc.perform(patch("/api/admin/orders/" + orderId + "/processing")
				.header("Authorization", "Bearer " + admin))
				.andExpect(status().isConflict());
	}

	@Test
	void eWalletPaymentFlow() throws Exception {
		String token = registerCustomer(uniqueEmail());
		String orderId = createOrder(token,
				"{\"items\":[{\"menuItemId\":\"MENU-005\",\"quantity\":1}]}");

		mvc.perform(post("/api/orders/" + orderId + "/payments/ewallet")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"provider\":\"PAYPAL\",\"accountNumber\":\"081234567890\"}"))
				.andExpect(status().isBadRequest());

		mvc.perform(post("/api/orders/" + orderId + "/payments/ewallet")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"provider\":\"DANA\",\"accountNumber\":\"081234567890\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.method").value("E-WALLET"))
				.andExpect(jsonPath("$.data.provider").value("DANA"))
				.andExpect(jsonPath("$.data.accountNumber", not("081234567890")));

		mvc.perform(get("/api/orders/" + orderId + "/payments")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("SUCCESS"));
	}

	@Test
	void cancelledOrderCannotBePaid() throws Exception {
		String token = registerCustomer(uniqueEmail());
		String orderId = createOrder(token,
				"{\"items\":[{\"menuItemId\":\"MENU-001\",\"quantity\":1}]}");

		mvc.perform(post("/api/orders/" + orderId + "/cancel")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("CANCELLED"));

		mvc.perform(post("/api/orders/" + orderId + "/payments/cash")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"cashGiven\":99999}"))
				.andExpect(status().isConflict());
	}

	@Test
	void orderItemSnapshotsSurviveMenuPriceChangeAndDeletion() throws Exception {
		String admin = adminToken();
		String token = registerCustomer(uniqueEmail());
		String orderId = createOrder(token,
				"{\"items\":[{\"menuItemId\":\"MENU-005\",\"quantity\":1}]}");

		MvcResult before = mvc.perform(get("/api/orders/" + orderId)
				.header("Authorization", "Bearer " + token)).andReturn();
		double subtotal = objectMapper.readTree(before.getResponse().getContentAsString())
				.at("/data/items/0/subtotal").asDouble();

		mvc.perform(patch("/api/menu/MENU-005/price")
				.header("Authorization", "Bearer " + admin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"price\":99000}"))
				.andExpect(status().isOk());

		// DELETE on referenced item soft-deletes it but the order is untouched
		mvc.perform(delete("/api/menu/MENU-005")
				.header("Authorization", "Bearer " + admin))
				.andExpect(status().isNoContent());
		mvc.perform(get("/api/menu/MENU-005")).andExpect(status().isNotFound());

		mvc.perform(get("/api/orders/" + orderId)
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.items[0].subtotal").value(subtotal));
	}

	@Test
	void adminDashboardReportsStats() throws Exception {
		String admin = adminToken();

		mvc.perform(get("/api/admin/orders")
				.header("Authorization", "Bearer " + admin))
				.andExpect(status().isOk());

		mvc.perform(get("/api/admin/dashboard")
				.header("Authorization", "Bearer " + admin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.totalOrders").isNumber())
				.andExpect(jsonPath("$.data.totalRevenue").isNumber());

		String customer = registerCustomer(uniqueEmail());
		mvc.perform(get("/api/admin/dashboard")
				.header("Authorization", "Bearer " + customer))
				.andExpect(status().isForbidden());
	}
}
