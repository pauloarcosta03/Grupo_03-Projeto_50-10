package com.grupo3.sasocial.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.grupo3.sasocial.domain.model.Beneficiario
import com.grupo3.sasocial.domain.model.DocumentoFile
import com.grupo3.sasocial.domain.model.Bem
import com.grupo3.sasocial.domain.model.Entrega
import com.grupo3.sasocial.domain.model.Alteracao
import com.grupo3.sasocial.domain.model.InventoryStatus
import com.grupo3.sasocial.domain.model.DeliveryStatus
import com.grupo3.sasocial.domain.model.TransactionType
import com.grupo3.sasocial.domain.model.GoodType
import com.grupo3.sasocial.domain.model.ApplicationStatus
import com.grupo3.sasocial.domain.model.Pedido
import com.grupo3.sasocial.domain.model.PedidoItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class FirestoreDataSource(
    private val firestore: FirebaseFirestore
) {
    // Coleções que correspondem ao website
    private val beneficiaryApplicationsCollection = firestore.collection("beneficiaryApplications")
    private val inventoryCollection = firestore.collection("inventory")
    private val deliveriesCollection = firestore.collection("deliveries")
    private val requestsCollection = firestore.collection("requests")
    private val usersCollection = firestore.collection("users")
    
    // Beneficiarios (candidaturas do website)
    fun getAllBeneficiarios(): Flow<List<Beneficiario>> {
        val stateFlow = MutableStateFlow<List<Beneficiario>>(emptyList())
        
        beneficiaryApplicationsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                
                val list = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        
                        // Parse files array
                        @Suppress("UNCHECKED_CAST")
                        val filesData = data["files"] as? List<Map<String, Any>> ?: emptyList()
                        val files = filesData.map { fileMap ->
                            DocumentoFile(
                                name = fileMap["name"] as? String ?: "",
                                type = fileMap["type"] as? String ?: "",
                                size = (fileMap["size"] as? Number)?.toLong() ?: 0L,
                                data = fileMap["data"] as? String ?: "",
                                documentoType = fileMap["documentoType"] as? String ?: "",
                                documentoLabel = fileMap["documentoLabel"] as? String ?: ""
                            )
                        }
                        
                        Beneficiario(
                            id = doc.id,
                            anoLetivo = data["anoLetivo"] as? String ?: "",
                            nome = data["nome"] as? String ?: "",
                            dataNascimento = data["dataNascimento"] as? String ?: "",
                            ccPassaporte = data["ccPassaporte"] as? String ?: "",
                            telemovel = data["telemovel"] as? String ?: "",
                            email = data["email"] as? String ?: "",
                            licenciatura = data["licenciatura"] as? Boolean ?: false,
                            mestrado = data["mestrado"] as? Boolean ?: false,
                            ctesp = data["ctesp"] as? Boolean ?: false,
                            curso = data["curso"] as? String ?: "",
                            numeroEstudante = data["numeroEstudante"] as? String ?: "",
                            produtosAlimentares = data["produtosAlimentares"] as? Boolean ?: false,
                            produtosHigienePessoal = data["produtosHigienePessoal"] as? Boolean ?: false,
                            produtosLimpeza = data["produtosLimpeza"] as? Boolean ?: false,
                            outros = data["outros"] as? Boolean ?: false,
                            apoiadoFAES = data["apoiadoFAES"] as? String ?: "",
                            beneficiarioBolsa = data["beneficiarioBolsa"] as? String ?: "",
                            entidadeValorBolsa = data["entidadeValorBolsa"] as? String ?: "",
                            declaracaoVeracidade = data["declaracaoVeracidade"] as? Boolean ?: false,
                            declaracaoRGPD = data["declaracaoRGPD"] as? Boolean ?: false,
                            data = data["data"] as? String ?: "",
                            assinatura = data["assinatura"] as? String ?: "",
                            createdAt = data["createdAt"] as? com.google.firebase.Timestamp,
                            status = data["status"] as? String ?: "pendente",
                            files = files
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                stateFlow.value = list
            }
        
        return stateFlow.asStateFlow()
    }
    
    suspend fun getAllBeneficiariosSync(): List<Beneficiario> {
        // Função síncrona para obter todas as candidaturas imediatamente
        // Usada quando precisamos dos dados imediatamente sem esperar pelo Flow
        return try {
            val snapshot = beneficiaryApplicationsCollection.get().await()
            android.util.Log.d("FirestoreDataSource", "getAllBeneficiariosSync: ${snapshot.documents.size} documentos encontrados")
            
            snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    
                    // Parse files array
                    @Suppress("UNCHECKED_CAST")
                    val filesData = data["files"] as? List<Map<String, Any>> ?: emptyList()
                    val files = filesData.map { fileMap ->
                        DocumentoFile(
                            name = fileMap["name"] as? String ?: "",
                            type = fileMap["type"] as? String ?: "",
                            size = (fileMap["size"] as? Number)?.toLong() ?: 0L,
                            data = fileMap["data"] as? String ?: "",
                            documentoType = fileMap["documentoType"] as? String ?: "",
                            documentoLabel = fileMap["documentoLabel"] as? String ?: ""
                        )
                    }
                    
                    Beneficiario(
                        id = doc.id,
                        anoLetivo = data["anoLetivo"] as? String ?: "",
                        nome = data["nome"] as? String ?: "",
                        dataNascimento = data["dataNascimento"] as? String ?: "",
                        ccPassaporte = data["ccPassaporte"] as? String ?: "",
                        telemovel = data["telemovel"] as? String ?: "",
                        email = data["email"] as? String ?: "",
                        licenciatura = data["licenciatura"] as? Boolean ?: false,
                        mestrado = data["mestrado"] as? Boolean ?: false,
                        ctesp = data["ctesp"] as? Boolean ?: false,
                        curso = data["curso"] as? String ?: "",
                        numeroEstudante = data["numeroEstudante"] as? String ?: "",
                        produtosAlimentares = data["produtosAlimentares"] as? Boolean ?: false,
                        produtosHigienePessoal = data["produtosHigienePessoal"] as? Boolean ?: false,
                        produtosLimpeza = data["produtosLimpeza"] as? Boolean ?: false,
                        outros = data["outros"] as? Boolean ?: false,
                        apoiadoFAES = data["apoiadoFAES"] as? String ?: "",
                        beneficiarioBolsa = data["beneficiarioBolsa"] as? String ?: "",
                        entidadeValorBolsa = data["entidadeValorBolsa"] as? String ?: "",
                        declaracaoVeracidade = data["declaracaoVeracidade"] as? Boolean ?: false,
                        declaracaoRGPD = data["declaracaoRGPD"] as? Boolean ?: false,
                        data = data["data"] as? String ?: "",
                        assinatura = data["assinatura"] as? String ?: "",
                        createdAt = data["createdAt"] as? com.google.firebase.Timestamp,
                        status = data["status"] as? String ?: "pendente",
                        files = files
                    )
                } catch (e: Exception) {
                    android.util.Log.e("FirestoreDataSource", "Error parsing Beneficiario ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("FirestoreDataSource", "Error getting beneficiarios sync", e)
            emptyList()
        }
    }
    
    suspend fun getBeneficiarioById(id: String): Beneficiario? {
        val doc = beneficiaryApplicationsCollection.document(id).get().await()
        val data = doc.data ?: return null
        
        @Suppress("UNCHECKED_CAST")
        val filesData = data["files"] as? List<Map<String, Any>> ?: emptyList()
        val files = filesData.map { fileMap ->
            DocumentoFile(
                name = fileMap["name"] as? String ?: "",
                type = fileMap["type"] as? String ?: "",
                size = (fileMap["size"] as? Number)?.toLong() ?: 0L,
                data = fileMap["data"] as? String ?: "",
                documentoType = fileMap["documentoType"] as? String ?: "",
                documentoLabel = fileMap["documentoLabel"] as? String ?: ""
            )
        }
        
        return Beneficiario(
            id = doc.id,
            anoLetivo = data["anoLetivo"] as? String ?: "",
            nome = data["nome"] as? String ?: "",
            dataNascimento = data["dataNascimento"] as? String ?: "",
            ccPassaporte = data["ccPassaporte"] as? String ?: "",
            telemovel = data["telemovel"] as? String ?: "",
            email = data["email"] as? String ?: "",
            licenciatura = data["licenciatura"] as? Boolean ?: false,
            mestrado = data["mestrado"] as? Boolean ?: false,
            ctesp = data["ctesp"] as? Boolean ?: false,
            curso = data["curso"] as? String ?: "",
            numeroEstudante = data["numeroEstudante"] as? String ?: "",
            produtosAlimentares = data["produtosAlimentares"] as? Boolean ?: false,
            produtosHigienePessoal = data["produtosHigienePessoal"] as? Boolean ?: false,
            produtosLimpeza = data["produtosLimpeza"] as? Boolean ?: false,
            outros = data["outros"] as? Boolean ?: false,
            apoiadoFAES = data["apoiadoFAES"] as? String ?: "",
            beneficiarioBolsa = data["beneficiarioBolsa"] as? String ?: "",
            entidadeValorBolsa = data["entidadeValorBolsa"] as? String ?: "",
            declaracaoVeracidade = data["declaracaoVeracidade"] as? Boolean ?: false,
            declaracaoRGPD = data["declaracaoRGPD"] as? Boolean ?: false,
            data = data["data"] as? String ?: "",
            assinatura = data["assinatura"] as? String ?: "",
            createdAt = data["createdAt"] as? com.google.firebase.Timestamp,
            status = data["status"] as? String ?: "pendente",
            files = files
        )
    }
    
    suspend fun updateBeneficiarioStatus(id: String, status: String) {
        beneficiaryApplicationsCollection.document(id)
            .update("status", status)
            .await()
    }
    
    // Inventory (Bens)
    fun getAllBens(): Flow<List<Bem>> {
        val stateFlow = MutableStateFlow<List<Bem>>(emptyList())
        
        inventoryCollection.addSnapshotListener { snapshot, _ ->
            val list = snapshot?.documents?.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    Bem(
                        id = doc.id,
                        name = data["name"] as? String ?: "",
                        category = data["category"] as? String ?: "",
                        goodTypeId = data["goodTypeId"] as? String ?: "",
                        quantity = (data["quantity"] as? Number)?.toInt() ?: 0,
                        minStock = (data["minStock"] as? Number)?.toInt() ?: 0,
                        supplier = data["supplier"] as? String ?: "",
                        locationId = data["locationId"] as? String ?: "",
                        statusId = data["statusId"] as? String ?: "",
                        createdAt = data["createdAt"] as? com.google.firebase.Timestamp,
                        entryDate = data["entryDate"] as? com.google.firebase.Timestamp,
                        validUntil = data["validUntil"] as? com.google.firebase.Timestamp
                    )
                } catch (e: Exception) {
                    android.util.Log.e("FirestoreDataSource", "Error parsing Bem ${doc.id}: ${e.message}", e)
                    null
                }
            } ?: emptyList()
            stateFlow.value = list
        }
        
        return stateFlow.asStateFlow()
    }
    
    suspend fun getBemById(id: String): Bem? {
        val doc = inventoryCollection.document(id).get().await()
        return try {
            val data = doc.data ?: return null
            Bem(
                id = doc.id,
                name = data["name"] as? String ?: "",
                category = data["category"] as? String ?: "",
                goodTypeId = data["goodTypeId"] as? String ?: "",
                quantity = (data["quantity"] as? Number)?.toInt() ?: 0,
                minStock = (data["minStock"] as? Number)?.toInt() ?: 0,
                supplier = data["supplier"] as? String ?: "",
                locationId = data["locationId"] as? String ?: "",
                statusId = data["statusId"] as? String ?: "",
                createdAt = data["createdAt"] as? com.google.firebase.Timestamp,
                entryDate = data["entryDate"] as? com.google.firebase.Timestamp,
                validUntil = data["validUntil"] as? com.google.firebase.Timestamp
            )
        } catch (e: Exception) {
            android.util.Log.e("FirestoreDataSource", "Error parsing Bem ${doc.id}: ${e.message}", e)
            null
        }
    }
    
    suspend fun createBem(bem: Bem): String {
        val data = hashMapOf<String, Any>(
            "name" to bem.name,
            "category" to bem.category,
            "goodTypeId" to bem.goodTypeId,
            "quantity" to bem.quantity,
            "minStock" to bem.minStock,
            "supplier" to bem.supplier,
            "locationId" to bem.locationId,
            "statusId" to bem.statusId
        )
        
        // Adiciona campos opcionais apenas se não forem null
        bem.createdAt?.let { data["createdAt"] = it }
        bem.entryDate?.let { data["entryDate"] = it }
        bem.validUntil?.let { 
            data["validUntil"] = it
            android.util.Log.d("FirestoreDataSource", "Saving validUntil: ${it.toDate()}")
        } ?: run {
            android.util.Log.d("FirestoreDataSource", "validUntil is null, not saving")
        }
        
        android.util.Log.d("FirestoreDataSource", "Creating Bem with data: $data")
        val docRef = inventoryCollection.add(data).await()
        android.util.Log.d("FirestoreDataSource", "Created Bem with ID: ${docRef.id}")
        return docRef.id
    }
    
    suspend fun updateBem(bem: Bem) {
        val data = hashMapOf<String, Any>(
            "name" to bem.name,
            "category" to bem.category,
            "goodTypeId" to bem.goodTypeId,
            "quantity" to bem.quantity,
            "minStock" to bem.minStock,
            "supplier" to bem.supplier,
            "locationId" to bem.locationId,
            "statusId" to bem.statusId
        )
        
        // Adiciona campos opcionais apenas se não forem null
        bem.createdAt?.let { data["createdAt"] = it }
        bem.entryDate?.let { data["entryDate"] = it }
        bem.validUntil?.let { 
            data["validUntil"] = it
            android.util.Log.d("FirestoreDataSource", "Updating validUntil: ${it.toDate()}")
        } ?: run {
            android.util.Log.d("FirestoreDataSource", "validUntil is null, will be removed")
        }
        
        android.util.Log.d("FirestoreDataSource", "Updating Bem ${bem.id} with data: $data")
        
        // Se validUntil for null, precisamos usar set com merge para remover o campo
        if (bem.validUntil == null) {
            // Primeiro atualiza todos os campos
            inventoryCollection.document(bem.id).update(data).await()
            // Depois remove o campo validUntil se existir
            inventoryCollection.document(bem.id).update("validUntil", FieldValue.delete()).await()
        } else {
            inventoryCollection.document(bem.id).update(data).await()
        }
    }
    
    suspend fun deleteBem(id: String) {
        inventoryCollection.document(id).delete().await()
    }
    
    // Deliveries (Entregas/Baixas)
    fun getAllEntregas(): Flow<List<Entrega>> {
        val stateFlow = MutableStateFlow<List<Entrega>>(emptyList())
        
        deliveriesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.e("FirestoreDataSource", "Error getting deliveries", error)
                return@addSnapshotListener
            }
            
            val list = snapshot?.documents?.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    
                    Entrega(
                        id = doc.id,
                        beneficiaryEmail = data["beneficiaryEmail"] as? String ?: "",
                        beneficiaryId = data["beneficiaryId"] as? String ?: "",
                        beneficiaryName = data["beneficiaryName"] as? String ?: "",
                        deliveryStatusId = data["deliveryStatusId"] as? String ?: "",
                        createdAt = data["createdAt"] as? com.google.firebase.Timestamp,
                        deliveryDate = data["deliveryDate"] as? com.google.firebase.Timestamp,
                        notes = data["notes"] as? String ?: "",
                        productCategory = data["productCategory"] as? String ?: "",
                        productId = data["productId"] as? String ?: "",
                        productName = data["productName"] as? String ?: "",
                        quantity = (data["quantity"] as? Number)?.toInt() ?: 0,
                        stockAfter = (data["stockAfter"] as? Number)?.toInt() ?: 0,
                        stockBefore = (data["stockBefore"] as? Number)?.toInt() ?: 0
                    )
                } catch (e: Exception) {
                    android.util.Log.e("FirestoreDataSource", "Error parsing delivery", e)
                    null
                }
            } ?: emptyList()
            
            android.util.Log.d("FirestoreDataSource", "Loaded ${list.size} deliveries")
            stateFlow.value = list
        }
        
        return stateFlow.asStateFlow()
    }
    
    suspend fun createEntrega(entrega: Entrega): String {
        val data = hashMapOf(
            "beneficiaryEmail" to entrega.beneficiaryEmail,
            "beneficiaryId" to entrega.beneficiaryId,
            "beneficiaryName" to entrega.beneficiaryName,
            "deliveryStatusId" to entrega.deliveryStatusId,
            "createdAt" to entrega.createdAt,
            "deliveryDate" to entrega.deliveryDate,
            "notes" to entrega.notes,
            "productCategory" to entrega.productCategory,
            "productId" to entrega.productId,
            "productName" to entrega.productName,
            "quantity" to entrega.quantity,
            "stockAfter" to entrega.stockAfter,
            "stockBefore" to entrega.stockBefore
        )
        val docRef = deliveriesCollection.add(data).await()
        return docRef.id
    }
    
    // Alteracoes (log de ações)
    fun getAllAlteracoes(): Flow<List<Alteracao>> {
        val stateFlow = MutableStateFlow<List<Alteracao>>(emptyList())
        
        firestore.collection("alteracoes").addSnapshotListener { snapshot, _ ->
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Alteracao::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            stateFlow.value = list
        }
        
        return stateFlow.asStateFlow()
    }
    
    suspend fun createAlteracao(alteracao: Alteracao): String {
        val docRef = firestore.collection("alteracoes").add(alteracao).await()
        return docRef.id
    }
    
    // Lookup Tables - Inventory Status
    suspend fun getAllInventoryStatuses(): List<InventoryStatus> {
        val snapshot = firestore.collection("inventory_status").get().await()
        return snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            InventoryStatus(
                id = doc.id,
                name = data["name"] as? String ?: "",
                description = data["description"] as? String ?: ""
            )
        }
    }
    
    // Lookup Tables - Delivery Status
    suspend fun getAllDeliveryStatuses(): List<DeliveryStatus> {
        val snapshot = firestore.collection("delivery_status").get().await()
        return snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            DeliveryStatus(
                id = doc.id,
                name = data["name"] as? String ?: "",
                description = data["description"] as? String ?: ""
            )
        }
    }
    
    // Lookup Tables - Transaction Type
    suspend fun getAllTransactionTypes(): List<TransactionType> {
        val snapshot = firestore.collection("transaction_type").get().await()
        return snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            TransactionType(
                id = doc.id,
                name = data["name"] as? String ?: "",
                description = data["description"] as? String ?: ""
            )
        }
    }
    
    // Lookup Tables - Good Type
    suspend fun getAllGoodTypes(): List<GoodType> {
        return try {
            android.util.Log.d("FirestoreDataSource", "Fetching good_types collection...")
            val snapshot = firestore.collection("good_types").get().await()
            android.util.Log.d("FirestoreDataSource", "Received ${snapshot.documents.size} documents from good_types")
            
            if (snapshot.documents.isEmpty()) {
                android.util.Log.w("FirestoreDataSource", "good_types collection is empty")
                return emptyList()
            }
            
            val result = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    val name = data["name"] as? String ?: ""
                    val description = data["description"] as? String ?: ""
                    
                    if (name.isEmpty()) {
                        android.util.Log.w("FirestoreDataSource", "Skipping good type with empty name: id=${doc.id}")
                        return@mapNotNull null
                    }
                    
                    android.util.Log.d("FirestoreDataSource", "Parsing good type: id=${doc.id}, name=$name, description=$description")
                    GoodType(
                        id = doc.id,
                        name = name,
                        description = description
                    )
                } catch (e: Exception) {
                    android.util.Log.e("FirestoreDataSource", "Error parsing good type document ${doc.id}: ${e.message}", e)
                    null
                }
            }
            android.util.Log.d("FirestoreDataSource", "Successfully parsed ${result.size} good types: ${result.map { it.name }}")
            result
        } catch (e: Exception) {
            android.util.Log.e("FirestoreDataSource", "Error fetching good_types: ${e.message}", e)
            e.printStackTrace()
            // Retorna lista vazia em vez de lançar exceção para permitir fallback
            emptyList()
        }
    }
    
    // Lookup Tables - Application Status
    suspend fun getAllApplicationStatuses(): List<ApplicationStatus> {
        val snapshot = firestore.collection("application_status").get().await()
        return snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            ApplicationStatus(
                id = doc.id,
                name = data["name"] as? String ?: "",
                description = data["description"] as? String ?: ""
            )
        }
    }
    
    // Requests (Pedidos de beneficiários)
    // Usar um StateFlow compartilhado para manter o listener ativo
    private val _allPedidosStateFlow = MutableStateFlow<List<Pedido>>(emptyList())
    private var pedidosListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var isListenerInitialized = false
    
    init {
        // Criar o listener uma vez no init
        android.util.Log.d("FirestoreDataSource", "=== INIT FirestoreDataSource ===")
        android.util.Log.d("FirestoreDataSource", "Inicializando listener para getAllPedidos()")
        initializePedidosListener()
    }
    
    private fun initializePedidosListener() {
        if (isListenerInitialized) {
            android.util.Log.d("FirestoreDataSource", "Listener já está inicializado, ignorando")
            return
        }
        
        android.util.Log.d("FirestoreDataSource", "Criando novo listener para requests collection")
        pedidosListenerRegistration = requestsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.e("FirestoreDataSource", "Error getting requests", error)
                return@addSnapshotListener
            }
            
            android.util.Log.d("FirestoreDataSource", "=== SNAPSHOT RECEBIDO ===")
            android.util.Log.d("FirestoreDataSource", "Total de documentos no snapshot: ${snapshot?.documents?.size ?: 0}")
            if (snapshot?.documents?.isEmpty() == true) {
                android.util.Log.w("FirestoreDataSource", "⚠️ Snapshot vazio - nenhum documento encontrado na coleção 'requests'")
            }
            
            val list = snapshot?.documents?.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    
                    android.util.Log.d("FirestoreDataSource", "Processando documento: ID=${doc.id}, Campos: ${data.keys}")
                    
                    @Suppress("UNCHECKED_CAST")
                    val itemsData = data["items"] as? List<Map<String, Any>> ?: emptyList()
                    val items = itemsData.map { itemMap ->
                        PedidoItem(
                            productId = itemMap["productId"] as? String ?: "",
                            productName = itemMap["productName"] as? String ?: "",
                            productCategory = itemMap["productCategory"] as? String ?: "",
                            quantity = (itemMap["quantity"] as? Number)?.toInt() ?: 0,
                            unit = itemMap["unit"] as? String ?: "unidade"
                        )
                    }
                    
                    val beneficiarioEmail = data["beneficiarioEmail"] as? String ?: ""
                    val beneficiarioId = data["beneficiarioId"] as? String ?: ""
                    val beneficiarioNome = data["beneficiarioNome"] as? String ?: ""
                    val status = data["status"] as? String ?: "PENDENTE"
                    
                    // Log detalhado para debug
                    if (beneficiarioEmail.isEmpty()) {
                        android.util.Log.w("FirestoreDataSource", "⚠️ Pedido ${doc.id} SEM beneficiarioEmail! BeneficiarioId=$beneficiarioId, Nome=$beneficiarioNome")
                    }
                    
                    android.util.Log.d("FirestoreDataSource", "Pedido carregado: ID=${doc.id}, Email='$beneficiarioEmail', Nome='$beneficiarioNome', Status=$status, Items=${items.size}")
                    
                    Pedido(
                        id = doc.id,
                        beneficiarioId = beneficiarioId,
                        beneficiarioEmail = beneficiarioEmail,
                        beneficiarioNome = beneficiarioNome,
                        items = items,
                        status = status,
                        observacoes = data["observacoes"] as? String ?: "",
                        createdAt = data["createdAt"] as? com.google.firebase.Timestamp,
                        updatedAt = data["updatedAt"] as? com.google.firebase.Timestamp,
                        aprovadoPor = data["aprovadoPor"] as? String,
                        dataAprovacao = data["dataAprovacao"] as? com.google.firebase.Timestamp,
                        dataEntrega = data["dataEntrega"] as? com.google.firebase.Timestamp
                    )
                } catch (e: Exception) {
                    android.util.Log.e("FirestoreDataSource", "❌ Error parsing request ${doc.id}", e)
                    android.util.Log.e("FirestoreDataSource", "Stack trace: ${e.stackTraceToString()}")
                    null
                }
            } ?: emptyList()
            
            android.util.Log.d("FirestoreDataSource", "Total de pedidos carregados: ${list.size}")
            android.util.Log.d("FirestoreDataSource", "Emails dos pedidos: ${list.map { it.beneficiarioEmail.trim().lowercase() }}")
            android.util.Log.d("FirestoreDataSource", "Status dos pedidos: ${list.map { it.status }}")
            
            // Atualizar o StateFlow compartilhado - isso vai notificar todos os collectors automaticamente
            _allPedidosStateFlow.value = list
            android.util.Log.d("FirestoreDataSource", "✅ StateFlow atualizado com ${list.size} pedidos - todos os collectors serão notificados")
        }
        isListenerInitialized = true
        android.util.Log.d("FirestoreDataSource", "✅ Listener criado e ativo no init")
    }
    
    fun getAllPedidos(): Flow<List<Pedido>> {
        android.util.Log.d("FirestoreDataSource", "getAllPedidos() chamado")
        android.util.Log.d("FirestoreDataSource", "Listener inicializado: $isListenerInitialized")
        android.util.Log.d("FirestoreDataSource", "Pedidos atuais no StateFlow: ${_allPedidosStateFlow.value.size}")
        
        // Garantir que o listener está ativo
        if (!isListenerInitialized) {
            android.util.Log.w("FirestoreDataSource", "⚠️ Listener não estava inicializado, inicializando agora")
            initializePedidosListener()
        }
        
        android.util.Log.d("FirestoreDataSource", "Retornando StateFlow compartilhado")
        return _allPedidosStateFlow.asStateFlow()
    }
    
    suspend fun createPedido(pedido: Pedido): String {
        val itemsData = pedido.items.map { item ->
            hashMapOf(
                "productId" to item.productId,
                "productName" to item.productName,
                "productCategory" to item.productCategory,
                "quantity" to item.quantity,
                "unit" to item.unit
            )
        }
        
        val data = hashMapOf(
            "beneficiarioId" to pedido.beneficiarioId,
            "beneficiarioEmail" to pedido.beneficiarioEmail,
            "beneficiarioNome" to pedido.beneficiarioNome,
            "items" to itemsData,
            "status" to pedido.status,
            "observacoes" to pedido.observacoes,
            "createdAt" to (pedido.createdAt ?: com.google.firebase.Timestamp.now()),
            "updatedAt" to com.google.firebase.Timestamp.now()
        )
        
        pedido.aprovadoPor?.let { data["aprovadoPor"] = it }
        pedido.dataAprovacao?.let { data["dataAprovacao"] = it }
        pedido.dataEntrega?.let { data["dataEntrega"] = it }
        
        android.util.Log.d("FirestoreDataSource", "=== CRIANDO PEDIDO ===")
        android.util.Log.d("FirestoreDataSource", "Email recebido: '${pedido.beneficiarioEmail}' (trimmed: '${pedido.beneficiarioEmail.trim()}', length=${pedido.beneficiarioEmail.length})")
        android.util.Log.d("FirestoreDataSource", "Nome: '${pedido.beneficiarioNome}'")
        android.util.Log.d("FirestoreDataSource", "ID: '${pedido.beneficiarioId}'")
        android.util.Log.d("FirestoreDataSource", "Status: ${pedido.status}, Items: ${itemsData.size}")
        
        // Verificar se o email está vazio ANTES de guardar
        if (pedido.beneficiarioEmail.isBlank()) {
            android.util.Log.e("FirestoreDataSource", "❌ ERRO CRÍTICO: Email está vazio! Não pode guardar pedido sem email.")
            throw IllegalArgumentException("Email do beneficiário não pode estar vazio")
        }
        
        android.util.Log.d("FirestoreDataSource", "Data a guardar: $data")
        android.util.Log.d("FirestoreDataSource", "Campo beneficiarioEmail no data: '${data["beneficiarioEmail"]}'")
        
        val docRef = requestsCollection.add(data).await()
        
        android.util.Log.d("FirestoreDataSource", "✅ Pedido guardado com sucesso! ID: ${docRef.id}")
        
        // Forçar atualização do listener - o listener deve receber a atualização automaticamente
        // mas aguardamos um pouco para garantir que o Firestore processou
        android.util.Log.d("FirestoreDataSource", "Aguardando processamento do Firestore...")
        kotlinx.coroutines.delay(1000) // Aumentar delay para dar tempo ao Firestore processar
        
        // Verificar se foi guardado corretamente
        val savedDoc = requestsCollection.document(docRef.id).get().await()
        if (savedDoc.exists()) {
            val savedData = savedDoc.data
            val savedEmail = savedData?.get("beneficiarioEmail") as? String ?: ""
            android.util.Log.d("FirestoreDataSource", "✅ Pedido verificado no Firestore:")
            android.util.Log.d("FirestoreDataSource", "  - Email guardado: '$savedEmail' (esperado: '${pedido.beneficiarioEmail}')")
            android.util.Log.d("FirestoreDataSource", "  - Nome guardado: '${savedData?.get("beneficiarioNome")}'")
            android.util.Log.d("FirestoreDataSource", "  - Status guardado: '${savedData?.get("status")}'")
            
            if (savedEmail.isBlank()) {
                android.util.Log.e("FirestoreDataSource", "❌ ERRO: Email não foi guardado no Firestore! Dados: $savedData")
            } else if (savedEmail.trim().lowercase() != pedido.beneficiarioEmail.trim().lowercase()) {
                android.util.Log.w("FirestoreDataSource", "⚠️ AVISO: Email guardado difere do esperado!")
            }
        } else {
            android.util.Log.e("FirestoreDataSource", "❌ ERRO: Pedido não existe após guardar!")
        }
        android.util.Log.d("FirestoreDataSource", "=== FIM DA CRIAÇÃO ===")
        
        return docRef.id
    }
    
    suspend fun updatePedidoStatus(pedidoId: String, status: String, aprovadoPor: String? = null) {
        android.util.Log.d("FirestoreDataSource", "=== ATUALIZANDO STATUS DO PEDIDO ===")
        android.util.Log.d("FirestoreDataSource", "Pedido ID: $pedidoId")
        android.util.Log.d("FirestoreDataSource", "Novo status: $status")
        android.util.Log.d("FirestoreDataSource", "Aprovado por: $aprovadoPor")
        
        // Verificar o status atual antes de atualizar
        val docBefore = requestsCollection.document(pedidoId).get().await()
        val statusAntes = docBefore.data?.get("status") as? String ?: "N/A"
        android.util.Log.d("FirestoreDataSource", "Status antes: $statusAntes")
        
        val updates = hashMapOf<String, Any>(
            "status" to status,
            "updatedAt" to com.google.firebase.Timestamp.now()
        )
        
        if (aprovadoPor != null) {
            updates["aprovadoPor"] = aprovadoPor
            updates["dataAprovacao"] = com.google.firebase.Timestamp.now()
        }
        
        if (status == "ENTREGUE") {
            updates["dataEntrega"] = com.google.firebase.Timestamp.now()
        }
        
        android.util.Log.d("FirestoreDataSource", "Updates a aplicar: $updates")
        
        requestsCollection.document(pedidoId).update(updates).await()
        
        // Verificar o status depois de atualizar
        val docAfter = requestsCollection.document(pedidoId).get().await()
        val statusDepois = docAfter.data?.get("status") as? String ?: "N/A"
        android.util.Log.d("FirestoreDataSource", "Status depois: $statusDepois")
        android.util.Log.d("FirestoreDataSource", "=== FIM DA ATUALIZAÇÃO ===")
    }
}
