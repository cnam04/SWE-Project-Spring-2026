package com.cpvt.prereq_visualizer.service;

import com.cpvt.prereq_visualizer.model.TestItem;
import com.cpvt.prereq_visualizer.repository.TestItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestItemServiceTest {

    // Creates a mock instance of TestItemRepository. 
    // This allows us to define its behavior without relying on a real database connection.
    @Mock
    private TestItemRepository testItemRepository;

    // Injects the mocked testItemRepository into the TestItemService instance.
    // This provides us with a service instance that has its dependencies mocked out.
    @InjectMocks
    private TestItemService testItemService;

    /**
     * Tests the getAllTestItems method of the TestItemService.
     * It ensures that the service properly integrates with the repository 
     * and returns the exact list of items provided by the repository.
     */
    @Test
    void getAllTestItems_ShouldReturnListOfTestItems() {
        // --- Arrange ---
        // 1. Create dummy data that our mock repository will return
        TestItem item1 = new TestItem();
        TestItem item2 = new TestItem();
        List<TestItem> mockItems = Arrays.asList(item1, item2);

        // 2. Define the behavior of the mocked repository: 
        // When the findAll() method is called, return our dummy list.
        when(testItemRepository.findAll()).thenReturn(mockItems);

        // --- Act ---
        // 3. Call the method being tested on the service
        List<TestItem> result = testItemService.getAllTestItems();

        // --- Assert ---
        // 4. Verify the results are what we expect
        assertNotNull(result, "The returned list should not be null");
        assertEquals(2, result.size(), "The size of the list should be 2, matching our mock setup");
        assertEquals(mockItems, result, "The returned list should match the mocked list exactly");
        
        // 5. Verify interactions: Ensure that the service actually called the required repository method
        verify(testItemRepository).findAll();
    }
}
