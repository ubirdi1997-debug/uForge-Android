package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.core.database.ForgeDatabase
import com.example.core.security.KeystoreManager
import com.example.data.ai.ForgeCodingAgentImpl
import com.example.data.auth.DemoAuthRepository
import com.example.data.projects.DemoProjectRepository
import com.example.feature.workspace.WorkspaceScreen
import com.example.feature.workspace.WorkspaceViewModel
import com.example.ui.theme.ForgeStudioTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = ForgeDatabase.getInstance(applicationContext)
        val keystoreManager = KeystoreManager(applicationContext)
        val authRepository = DemoAuthRepository(applicationContext, keystoreManager)
        val projectRepository = DemoProjectRepository(keystoreManager, database)
        val codingAgent = ForgeCodingAgentImpl()

        val viewModel = WorkspaceViewModel(
            projectRepository = projectRepository,
            authRepository = authRepository,
            codingAgent = codingAgent
        )

        setContent {
            ForgeStudioTheme {
                WorkspaceScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
