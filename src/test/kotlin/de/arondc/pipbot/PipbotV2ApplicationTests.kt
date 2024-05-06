package de.arondc.pipbot

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter

@SpringBootTest
class PipbotV2ApplicationTests {

    @Test
    fun contextLoads() {
    }

    @Test
    @Disabled("Should not run in CI currently")
    fun createApplicationModuleModelUMLs() {
        val modules: ApplicationModules = ApplicationModules.of(PipbotApplication::class.java)
        Documenter(modules)
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml()
            .writeModuleCanvases()

    }

    @Test
    fun verifyApplicationModuleModel() {
        val modules: ApplicationModules = ApplicationModules.of(PipbotApplication::class.java)
        modules.verify().forEach { println(it) }
    }

}
