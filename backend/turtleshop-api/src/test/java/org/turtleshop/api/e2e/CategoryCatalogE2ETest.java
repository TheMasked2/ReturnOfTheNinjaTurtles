package org.turtleshop.api.e2e;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.turtleshop.api.config.IntegrationTestBase;
import org.turtleshop.api.modules.category.dto.CreateCategoryRequest;
import org.turtleshop.api.modules.category.dto.UpdateCategoryRequest;

import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class CategoryCatalogE2ETest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void categoryReadEndpoints_withAuthenticatedUser_shouldReturnSeededCategories() throws Exception {
        mockMvc.perform(get("/api/categories").with(user("visitor@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists());

        mockMvc.perform(get("/api/categories/1").with(user("visitor@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Comic Books"));
    }

    @Test
    void categoryCrudEndpoints_withAdminAuthority_shouldCreateUpdateAndDeleteCategory() throws Exception {
        CreateCategoryRequest createRequest = new CreateCategoryRequest();
        createRequest.setName("E2E Category");
        createRequest.setDescription("Created from an e2e test");

        String createdJson = mockMvc.perform(post("/api/categories")
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("CATEGORY_CREATE_ALL")
                        ))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("E2E Category"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> createdCategory = objectMapper.readValue(createdJson, new TypeReference<>() {});
        int categoryId = (Integer) createdCategory.get("id");

        UpdateCategoryRequest updateRequest = new UpdateCategoryRequest();
        updateRequest.setName("Updated E2E Category");
        updateRequest.setDescription("Updated from an e2e test");

        mockMvc.perform(put("/api/categories/{id}", categoryId)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("CATEGORY_UPDATE_ALL")
                        ))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.name").value("Updated E2E Category"));

        mockMvc.perform(delete("/api/categories/{id}", categoryId)
                        .with(user("admin@turtleshop.org").authorities(
                                new SimpleGrantedAuthority("CATEGORY_DELETE_ALL")
                        )))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/categories/{id}", categoryId)
                        .with(user("visitor@example.com")))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCategoryEndpoint_withoutCategoryAuthority_shouldReturnForbidden() throws Exception {
        CreateCategoryRequest createRequest = new CreateCategoryRequest();
        createRequest.setName("Forbidden Category");
        createRequest.setDescription("This should not be created");

        mockMvc.perform(post("/api/categories")
                        .with(user("visitor@example.com"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }
}
