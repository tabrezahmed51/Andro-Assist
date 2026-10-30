package com.giyhub.app;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ModelCatalogTest {

    @Test
    public void testAvailableModelsIntegrity() {
        List<ModelCatalog.ModelItem> models = ModelCatalog.getAvailableModels();
        assertNotNull(models);
        assertTrue(models.size() >= 4);

        ModelCatalog.ModelItem qwen = models.get(0);
        assertEquals("qwen-0.5b", qwen.id);
        assertTrue(qwen.hfUrl.contains("huggingface.co"));
        assertTrue(qwen.fileName.endsWith(".gguf"));
        assertTrue(qwen.sizeMb > 0);

        ModelCatalog.ModelItem smollm = models.get(1);
        assertEquals("smollm-135m", smollm.id);
        assertTrue(smollm.hfUrl.contains("smollm2-135m-instruct-q4_k_m.gguf"));
    }
}
