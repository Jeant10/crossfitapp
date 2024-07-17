package com.jeantituana2024.tesis


import android.app.ProgressDialog
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.gson.Gson
import com.jeantituana2024.tesis.api.Api
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.auth.RegisterActivity
import com.jeantituana2024.tesis.models.RegisterRequest
import com.jeantituana2024.tesis.models.RegisterResponse
import com.jeantituana2024.tesis.models.UserRegister
import junit.framework.TestCase.assertEquals
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Method

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Config.OLDEST_SDK]) // Configura la versión de SDK deseada
class RegisterActivityTest {
//
//    // Mocks necesarios
//    @Mock
//    lateinit var mockCall: Call<RegisterResponse>
//
//    @Mock
//    lateinit var mockResponse: Response<RegisterResponse>
//
//    @Mock
//    lateinit var mockApi: Api
//
//    @Mock
//    lateinit var progressDialog: ProgressDialog // Agregar un mock para el ProgressDialog
//
//    @Before
//    fun setUp() {
//        // Inicializar mocks
//        MockitoAnnotations.openMocks(this)
//
//        RetrofitClient.setInstanceForTesting(mockApi)
//
//        // Configurar el contexto de la aplicación para Robolectric
//        val context = ApplicationProvider.getApplicationContext<Context>()
//        // Configurar contexto para los métodos que lo requieran
//        doReturn(context).`when`(context).applicationContext
//    }
//
//    @Test
//    fun testRegisterUser_SuccessfulResponse() {
//        // Simulación de datos
//        val userData = RegisterRequest("John Doe", "john.doe@example.com", "password")
//        val successResponse = RegisterResponse("Successfully Register", UserRegister("John Doe", "john.doe@example.com"), "3279d7b6-2be3-4eba-bc49-353e3b55fa38")
//
//        // Configuración del mock para el método register() del mockApi
//        `when`(mockApi.register(userData)).thenReturn(mockCall)
//
//        // Configuración del mock para la respuesta exitosa
//        `when`(mockResponse.isSuccessful).thenReturn(true)
//        `when`(mockResponse.body()).thenReturn(successResponse)
//        `when`(mockCall.enqueue(any())).thenAnswer {
//            val callback = it.arguments[0] as Callback<RegisterResponse>
//            callback.onResponse(mockCall, mockResponse)
//        }
//
//        // Crear una instancia de RegisterActivity (usando el contexto mockeado y el progressDialog mockeado)
//        val activity = spy(RegisterActivity())
//        activity.progressDialog = progressDialog
//
//        // Llamar al método bajo prueba
//        activity.registerUser()
//
//        // Verificaciones esperadas
//        verify(mockApi).register(userData)
//        verify(mockResponse).body()
//    }
//
//    @Test
//    fun testRegisterUser_ErrorResponse() {
//        // Simulación de datos
//        val userData = RegisterRequest("John Doe", "john.doe@example.com", "password")
//        val errorResponseBody = ResponseBody.create(null, "Error de validación")
//        val errorResponse = Response.error<RegisterResponse>(400, errorResponseBody)
//
//        // Configuración del mock para el método register() del mockApi
//        `when`(mockApi.register(userData)).thenReturn(mockCall)
//
//        // Configuración del mock para la respuesta de error
//        `when`(mockResponse.isSuccessful).thenReturn(false)
//        `when`(mockResponse.errorBody()).thenReturn(errorResponseBody)
//        `when`(mockResponse.code()).thenReturn(400)
//        `when`(mockCall.enqueue(any())).thenAnswer {
//            val callback = it.arguments[0] as Callback<RegisterResponse>
//            callback.onResponse(mockCall, errorResponse)
//        }
//
//        // Crear una instancia de RegisterActivity (usando el contexto mockeado y el progressDialog mockeado)
//        val activity = spy(RegisterActivity())
//        activity.progressDialog = progressDialog
//
//        // Llamar al método bajo prueba
//        activity.registerUser()
//
//        // Verificaciones esperadas
//        verify(mockApi).register(userData)
//        verify(mockResponse).errorBody()
//    }
//
//    // Utilidad para Mockito para aceptar cualquier Callback
//    private fun <T> any(): T {
//        return org.mockito.ArgumentMatchers.any()
//    }
}