package de.arondc.pipbot

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
    fun createApplicationModuleModelUMLs() {
        val modules: ApplicationModules = ApplicationModules.of(PipbotV2Application::class.java)
        Documenter(modules)
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml()
            .writeModuleCanvases()

    }

    @Test
    fun verifyApplicationModuleModel() {
        val modules: ApplicationModules = ApplicationModules.of(PipbotV2Application::class.java)
        modules.verify().forEach { println(it) }
    }

}
