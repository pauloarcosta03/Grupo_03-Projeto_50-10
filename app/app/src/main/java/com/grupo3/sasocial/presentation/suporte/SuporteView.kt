package com.grupo3.sasocial.presentation.suporte

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grupo3.sasocial.presentation.components.BottomNavBar
import com.grupo3.sasocial.ui.theme.*

data class FAQItem(
    val id: String,
    val pergunta: String,
    val resposta: List<String> // Lista de passos
)

@Composable
fun SuporteView(
    onNavigate: (String) -> Unit,
    isBeneficiario: Boolean = false
) {
    val faqs = remember {
        if (isBeneficiario) {
            // FAQs específicas para beneficiários
            listOf(
                FAQItem(
                    id = "criar_pedido",
                    pergunta = "Como criar um pedido?",
                    resposta = listOf(
                        "1. Vai ao menu 'Pedidos' no fundo do ecrã",
                        "2. Clica no botão '+' (canto superior direito) para criar novo pedido",
                        "3. Vê a lista de produtos disponíveis nas tuas categorias aceites",
                        "4. Seleciona os produtos que queres pedir",
                        "5. Usa os botões '+' e '-' para definir a quantidade de cada produto",
                        "6. (Opcional) Adiciona observações no campo de notas",
                        "7. Clica em 'Confirmar' para submeter o pedido",
                        "8. O teu pedido ficará com status 'Pendente' até ser aprovado pelo administrador",
                        "9. Serás redirecionado automaticamente para o Dashboard após submeter"
                    )
                ),
                FAQItem(
                    id = "ver_pedidos",
                    pergunta = "Como ver os meus pedidos?",
                    resposta = listOf(
                        "1. Vai ao menu 'Pedidos' no fundo do ecrã",
                        "2. Vês todos os teus pedidos listados com estatísticas no topo:",
                        "   - Total de pedidos",
                        "   - Pedidos pendentes",
                        "   - Pedidos aprovados",
                        "   - Pedidos rejeitados",
                        "   - Pedidos entregues",
                        "3. Cada pedido mostra:",
                        "   - ID do pedido",
                        "   - Status (Pendente, Aprovado, Rejeitado, Entregue)",
                        "   - Data de criação",
                        "   - Lista de itens pedidos com quantidades",
                        "   - Total de unidades",
                        "4. Os pedidos estão ordenados por data (mais recentes primeiro)"
                    )
                ),
                FAQItem(
                    id = "ver_stock",
                    pergunta = "Como ver o stock disponível?",
                    resposta = listOf(
                        "1. Vai ao menu 'Stock' no fundo do ecrã",
                        "2. Vês todos os produtos disponíveis nas tuas categorias aceites",
                        "3. Cada produto mostra:",
                        "   - Nome do produto",
                        "   - Categoria",
                        "   - Quantidade disponível em stock",
                        "   - Fornecedor",
                        "   - Data de entrada e validade (se aplicável)",
                        "4. Só podes ver produtos das categorias que foram aceites na tua candidatura",
                        "5. Se não vês nenhum produto, verifica as tuas 'Categorias Aceites' no Dashboard"
                    )
                ),
                FAQItem(
                    id = "status_pedido",
                    pergunta = "O que significam os diferentes status dos pedidos?",
                    resposta = listOf(
                        "📋 PENDENTE: O teu pedido foi submetido e está à espera de aprovação pelo administrador. Podes ver o número de pedidos pendentes no teu Dashboard.",
                        "",
                        "✅ APROVADO: O administrador aprovou o teu pedido e o stock foi reservado. O pedido está pronto para ser entregue.",
                        "",
                        "❌ REJEITADO: O teu pedido foi rejeitado pelo administrador. Podes ver os detalhes na secção 'Pedidos' e criar um novo pedido se necessário.",
                        "",
                        "📦 ENTREGUE: O teu pedido foi entregue e está completo. O processo terminou com sucesso."
                    )
                ),
                FAQItem(
                    id = "categorias_aceites",
                    pergunta = "Quais são as minhas categorias aceites?",
                    resposta = listOf(
                        "1. Vai ao teu Dashboard (menu 'Início')",
                        "2. Na secção 'Categorias Aceites' vês as categorias que podes aceder:",
                        "   - Alimentos",
                        "   - Higiene Pessoal",
                        "   - Limpeza",
                        "   - Outros",
                        "3. Só podes ver e pedir produtos destas categorias",
                        "4. Se tiveres todas as categorias marcadas, tens acesso a todos os produtos",
                        "5. As categorias são definidas quando a tua candidatura é aprovada",
                        "6. Se não vês nenhuma categoria, contacta o suporte"
                    )
                ),
                FAQItem(
                    id = "dashboard",
                    pergunta = "O que posso ver no Dashboard?",
                    resposta = listOf(
                        "O Dashboard é a tua página inicial e mostra:",
                        "",
                        "📊 Estatísticas (atualizadas automaticamente):",
                        "   - Total de pedidos que já criaste",
                        "   - Pedidos pendentes (aguardando aprovação)",
                        "   - Pedidos aprovados (confirmados)",
                        "",
                        "📋 Categorias Aceites:",
                        "   - Lista das categorias de produtos que podes pedir",
                        "   - Só podes ver e pedir produtos destas categorias",
                        "",
                        "📦 Pedidos Recentes:",
                        "   - Os teus pedidos mais recentes com status",
                        "   - Atualizados automaticamente quando há mudanças",
                        "",
                        "🚪 Botão Sair:",
                        "   - Termina a sessão e volta ao ecrã de login",
                        "",
                        "💡 Tudo atualiza automaticamente - não precisas de fazer nada!"
                    )
                ),
                FAQItem(
                    id = "problema_pedido",
                    pergunta = "O meu pedido foi rejeitado, o que fazer?",
                    resposta = listOf(
                        "Se o teu pedido foi rejeitado:",
                        "",
                        "1. Vê os detalhes do pedido rejeitado na secção 'Pedidos'",
                        "2. Verifica se há alguma observação do administrador",
                        "3. Possíveis razões para rejeição:",
                        "   - Stock insuficiente no momento da aprovação",
                        "   - Produto deixou de estar disponível",
                        "   - Quantidade pedida excedia o stock disponível",
                        "",
                        "4. O que podes fazer:",
                        "   - Verificar o stock atual na secção 'Stock'",
                        "   - Criar um novo pedido com produtos diferentes ou quantidades menores",
                        "   - Aguardar que o stock seja reposto",
                        "",
                        "5. Se tiveres dúvidas, contacta o suporte através desta página"
                    )
                ),
                FAQItem(
                    id = "pedido_aprovado",
                    pergunta = "O meu pedido foi aprovado, o que acontece agora?",
                    resposta = listOf(
                        "Quando o teu pedido é aprovado:",
                        "",
                        "1. O stock dos produtos foi reservado para ti",
                        "2. O status do pedido muda para 'Aprovado'",
                        "3. Podes ver o pedido aprovado na secção 'Pedidos'",
                        "4. O administrador irá preparar a entrega",
                        "5. Quando o pedido for entregue, o status muda para 'Entregue'",
                        "",
                        "📊 As estatísticas no Dashboard são atualizadas automaticamente",
                        "",
                        "💡 Dica: Podes criar novos pedidos enquanto tens pedidos aprovados pendentes"
                    )
                ),
                FAQItem(
                    id = "atualizar_dados",
                    pergunta = "Como atualizar as informações do Dashboard?",
                    resposta = listOf(
                        "O Dashboard atualiza automaticamente em tempo real:",
                        "",
                        "💡 O Dashboard atualiza automaticamente quando:",
                        "   - Criares um novo pedido",
                        "   - O status de um pedido mudar (aprovado, rejeitado, entregue)",
                        "   - Voltares ao Dashboard após navegar para outras páginas",
                        "   - O administrador aprovar ou rejeitar um dos teus pedidos",
                        "",
                        "📊 As estatísticas são atualizadas instantaneamente:",
                        "   - Total de pedidos",
                        "   - Pedidos pendentes",
                        "   - Pedidos aprovados",
                        "   - Pedidos recentes",
                        "",
                        "Não é necessário fazer nada - tudo atualiza automaticamente!"
                    )
                ),
                FAQItem(
                    id = "contactar_suporte",
                    pergunta = "Como contactar o suporte?",
                    resposta = listOf(
                        "Estás na página de Suporte e Ajuda:",
                        "",
                        "1. Lê todas as perguntas frequentes acima",
                        "2. Cada pergunta tem uma resposta detalhada passo a passo",
                        "3. Se não encontrares a resposta à tua questão:",
                        "   - Verifica todas as perguntas disponíveis",
                        "   - Expande cada pergunta para ver a resposta completa",
                        "",
                        "4. Para questões urgentes ou problemas técnicos:",
                        "   - Email: grupo3.dev.firebase@gmail.com",
                        "   - Indica o teu email e descreve o problema",
                        "",
                        "5. Informações úteis para incluir no contacto:",
                        "   - O teu email de beneficiário",
                        "   - Descrição do problema ou questão",
                        "   - Screenshots se aplicável"
                    )
                )
            )
        } else {
            // FAQs específicas para administradores
            listOf(
                FAQItem(
                    id = "gerir_candidaturas",
                    pergunta = "Como gerir candidaturas de beneficiários?",
                    resposta = listOf(
                        "1. Vai ao menu 'Beneficiários' no fundo do ecrã",
                        "2. Vês todas as candidaturas submetidas pelos beneficiários",
                        "3. Clica numa candidatura para ver os detalhes:",
                        "   - Informações pessoais do candidato",
                        "   - Documentos anexados",
                        "   - Categorias selecionadas",
                        "   - Status atual da candidatura",
                        "",
                        "4. Para aprovar uma candidatura:",
                        "   - Clica no botão 'Aprovar'",
                        "   - O sistema criará automaticamente uma conta Firebase para o beneficiário",
                        "   - Será enviado um email com credenciais de acesso",
                        "",
                        "5. Para rejeitar uma candidatura:",
                        "   - Clica no botão 'Rejeitar'",
                        "   - A candidatura ficará com status 'Recusado'",
                        "",
                        "6. Para criar conta de beneficiário já aprovado:",
                        "   - Se a candidatura foi aprovada pelo website",
                        "   - Clica em 'Criar Conta para Beneficiário'",
                        "   - O sistema criará a conta e enviará as credenciais"
                    )
                ),
                FAQItem(
                    id = "aprovar_pedidos",
                    pergunta = "Como aprovar ou rejeitar pedidos de beneficiários?",
                    resposta = listOf(
                        "1. Vai ao menu 'Pedidos' no fundo do ecrã",
                        "2. Usa os filtros no topo para ver:",
                        "   - Todos os pedidos",
                        "   - Pedidos pendentes (aguardando aprovação)",
                        "   - Pedidos aprovados",
                        "   - Pedidos rejeitados",
                        "   - Pedidos entregues",
                        "",
                        "3. Clica num pedido para ver os detalhes:",
                        "   - Informações do beneficiário",
                        "   - Lista de produtos pedidos com quantidades",
                        "   - Observações do beneficiário",
                        "   - Status atual",
                        "",
                        "4. Para aprovar um pedido:",
                        "   - Clica em 'Aprovar e Dar Baixa'",
                        "   - O stock será automaticamente reduzido",
                        "   - Será criado um registo de entrega",
                        "   - O pedido ficará com status 'Aprovado'",
                        "",
                        "5. Para rejeitar um pedido:",
                        "   - Clica em 'Rejeitar'",
                        "   - O pedido ficará com status 'Rejeitado'",
                        "   - O stock não será alterado",
                        "",
                        "⚠️ Nota: Não podes aprovar o mesmo pedido duas vezes"
                    )
                ),
                FAQItem(
                    id = "dar_baixa",
                    pergunta = "Como dar baixa e marcar pedido como entregue?",
                    resposta = listOf(
                        "Quando aprovas um pedido:",
                        "",
                        "1. O sistema faz automaticamente:",
                        "   - Reduz o stock dos produtos pedidos",
                        "   - Cria um registo de entrega",
                        "   - Atualiza o status do pedido para 'Aprovado'",
                        "",
                        "2. Para marcar como entregue:",
                        "   - Vai aos detalhes do pedido aprovado",
                        "   - Clica em 'Marcar como Entregue'",
                        "   - O pedido ficará com status 'Entregue'",
                        "",
                        "3. Ver histórico de entregas:",
                        "   - Vai ao menu 'Histórico'",
                        "   - Vês todas as entregas realizadas",
                        "   - Podes ver estatísticas e detalhes de cada entrega",
                        "",
                        "💡 Dica: As entregas são registadas automaticamente quando aprovas um pedido"
                    )
                ),
                FAQItem(
                    id = "gerir_stock",
                    pergunta = "Como gerir o stock de produtos?",
                    resposta = listOf(
                        "1. Vai ao menu 'Stock' no fundo do ecrã",
                        "2. Vês todos os produtos em stock",
                        "",
                        "3. Para adicionar um novo produto:",
                        "   - Clica no botão '+' (FloatingActionButton)",
                        "   - Preenche os campos:",
                        "     • Nome do produto",
                        "     • Categoria (dropdown)",
                        "     • Quantidade",
                        "     • Fornecedor",
                        "     • Data de entrada",
                        "     • Data de validade (se aplicável)",
                        "     • Status (dropdown)",
                        "   - Clica em 'Adicionar'",
                        "",
                        "4. Para editar um produto:",
                        "   - Clica no produto na lista",
                        "   - Modifica os campos necessários",
                        "   - Clica em 'Guardar'",
                        "",
                        "5. Para eliminar um produto:",
                        "   - Clica no produto na lista",
                        "   - Clica em 'Eliminar'",
                        "   - Confirma a eliminação",
                        "",
                        "6. Filtros disponíveis:",
                        "   - Por categoria",
                        "   - Por status",
                        "   - Por fornecedor"
                    )
                ),
                FAQItem(
                    id = "dashboard_admin",
                    pergunta = "O que posso ver no Dashboard?",
                    resposta = listOf(
                        "O Dashboard mostra estatísticas e informações gerais:",
                        "",
                        "📊 Estatísticas principais:",
                        "   - Total de produtos em stock",
                        "   - Total de pedidos",
                        "   - Pedidos pendentes",
                        "   - Total de entregas",
                        "",
                        "📈 Gráficos:",
                        "   - Distribuição de produtos por categoria",
                        "   - Entregas por mês",
                        "   - Produtos mais pedidos",
                        "",
                        "📋 Tabela de produtos:",
                        "   - Lista de todos os produtos em stock",
                        "   - Podes filtrar por beneficiário ou categoria",
                        "   - Mostra quantidade, categoria e status",
                        "",
                        "💡 Tudo atualiza automaticamente em tempo real"
                    )
                ),
                FAQItem(
                    id = "historico_entregas",
                    pergunta = "Como ver o histórico de entregas?",
                    resposta = listOf(
                        "1. Vai ao menu 'Histórico' no fundo do ecrã",
                        "2. Vês um resumo no topo:",
                        "   - Total de entregas",
                        "   - Total de unidades entregues",
                        "   - Valor total entregue",
                        "",
                        "3. Tabela de entregas:",
                        "   - Data da entrega",
                        "   - Beneficiário",
                        "   - Produtos entregues",
                        "   - Quantidades",
                        "   - Status",
                        "",
                        "4. Podes ver detalhes completos de cada entrega",
                        "",
                        "💡 O histórico é atualizado automaticamente quando aprovas pedidos"
                    )
                ),
                FAQItem(
                    id = "filtrar_pedidos",
                    pergunta = "Como filtrar pedidos?",
                    resposta = listOf(
                        "Na página 'Pedidos' tens vários filtros:",
                        "",
                        "1. Filtros por status:",
                        "   - Todos: Mostra todos os pedidos",
                        "   - Pendentes: Apenas pedidos aguardando aprovação",
                        "   - Aprovados: Pedidos já aprovados",
                        "   - Rejeitados: Pedidos rejeitados",
                        "   - Entregues: Pedidos já entregues",
                        "",
                        "2. Cada filtro mostra o número de pedidos:",
                        "   - Exemplo: 'Pendentes (5)' significa 5 pedidos pendentes",
                        "",
                        "3. Para ver detalhes:",
                        "   - Clica num pedido da lista",
                        "   - Vês todas as informações e podes aprovar/rejeitar",
                        "",
                        "💡 Os pedidos estão ordenados por data (mais recentes primeiro)"
                    )
                ),
                FAQItem(
                    id = "problema_aprovacao",
                    pergunta = "Não consigo aprovar um pedido, o que fazer?",
                    resposta = listOf(
                        "Possíveis causas e soluções:",
                        "",
                        "1. Pedido já foi aprovado:",
                        "   - Verifica o status do pedido",
                        "   - Se já está 'Aprovado' ou 'Entregue', não podes aprovar novamente",
                        "   - Isto previne duplicação de baixas no stock",
                        "",
                        "2. Stock insuficiente:",
                        "   - Verifica se há stock suficiente para todos os produtos",
                        "   - Se não houver, rejeita o pedido ou aguarda reposição",
                        "",
                        "3. Erro ao processar:",
                        "   - Verifica a ligação à internet",
                        "   - Tenta novamente após alguns segundos",
                        "   - Se o problema persistir, contacta o suporte",
                        "",
                        "4. Pedido não está pendente:",
                        "   - Só podes aprovar pedidos com status 'Pendente'",
                        "   - Verifica o filtro de status na lista de pedidos"
                    )
                ),
                FAQItem(
                    id = "criar_conta_beneficiario",
                    pergunta = "Como criar conta para beneficiário aprovado?",
                    resposta = listOf(
                        "Se uma candidatura foi aprovada pelo website:",
                        "",
                        "1. Vai ao menu 'Beneficiários'",
                        "2. Encontra a candidatura aprovada",
                        "3. Clica nos detalhes da candidatura",
                        "4. Se a candidatura está 'Aceite' ou 'Aprovada':",
                        "   - Vês o botão 'Criar Conta para Beneficiário'",
                        "   - Clica no botão",
                        "",
                        "5. O sistema irá:",
                        "   - Criar uma conta Firebase Auth para o beneficiário",
                        "   - Gerar uma palavra-passe temporária",
                        "   - Enviar um email com as credenciais de acesso",
                        "",
                        "6. O beneficiário poderá:",
                        "   - Fazer login com o email da candidatura",
                        "   - Usar a palavra-passe temporária",
                        "   - Ser redirecionado para redefinir a palavra-passe",
                        "",
                        "💡 Nota: Se a candidatura foi aprovada pela app, a conta é criada automaticamente"
                    )
                ),
                FAQItem(
                    id = "status_pedidos_admin",
                    pergunta = "O que significam os diferentes status dos pedidos?",
                    resposta = listOf(
                        "📋 PENDENTE:",
                        "   - Pedido submetido pelo beneficiário",
                        "   - Aguardando aprovação do administrador",
                        "   - Stock ainda não foi reservado",
                        "",
                        "✅ APROVADO:",
                        "   - Pedido aprovado pelo administrador",
                        "   - Stock foi reduzido automaticamente",
                        "   - Registo de entrega foi criado",
                        "   - Aguardando entrega física",
                        "",
                        "❌ REJEITADO:",
                        "   - Pedido rejeitado pelo administrador",
                        "   - Stock não foi alterado",
                        "   - Beneficiário pode criar novo pedido",
                        "",
                        "📦 ENTREGUE:",
                        "   - Pedido foi entregue ao beneficiário",
                        "   - Processo completo",
                        "   - Aparece no histórico de entregas"
                    )
                ),
                FAQItem(
                    id = "contactar_suporte_admin",
                    pergunta = "Como contactar o suporte técnico?",
                    resposta = listOf(
                        "Estás na página de Suporte e Ajuda:",
                        "",
                        "1. Lê todas as perguntas frequentes acima",
                        "2. Cada pergunta tem uma resposta detalhada passo a passo",
                        "3. Se não encontrares a resposta à tua questão:",
                        "   - Verifica todas as perguntas disponíveis",
                        "   - Expande cada pergunta para ver a resposta completa",
                        "",
                        "4. Para questões urgentes ou problemas técnicos:",
                        "   - Email: grupo3.dev.firebase@gmail.com",
                        "   - Indica o teu email de administrador",
                        "   - Descreve o problema detalhadamente",
                        "",
                        "5. Informações úteis para incluir no contacto:",
                        "   - O teu email de administrador",
                        "   - Descrição detalhada do problema",
                        "   - Screenshots se aplicável",
                        "   - Passos para reproduzir o problema",
                        "",
                        "6. Para questões sobre:",
                        "   - Funcionalidades da aplicação",
                        "   - Problemas com aprovações",
                        "   - Erros no sistema",
                        "   - Sugestões de melhorias"
                    )
                )
            )
        }
    }
    
    var expandedItemId by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = if (isBeneficiario) "beneficiarioSuporte" else "suporte",
                onNavigate = onNavigate,
                isBeneficiario = isBeneficiario
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SASBackground)
                .padding(paddingValues)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SASGreen)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Suporte e Ajuda",
                        color = SASWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.Help,
                        contentDescription = "Ajuda",
                        tint = SASWhite,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            // Introdução
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = SASWhite),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Bem-vindo ao Centro de Ajuda",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SASGreenDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Encontra respostas às perguntas mais frequentes sobre como usar a aplicação. Clica numa pergunta para ver a resposta passo a passo.",
                        fontSize = 14.sp,
                        color = SASGray,
                        lineHeight = 20.sp
                    )
                }
            }
            
            // Lista de FAQs
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(faqs) { faq ->
                    FAQCard(
                        faq = faq,
                        isExpanded = expandedItemId == faq.id,
                        onExpandedChange = { expanded ->
                            expandedItemId = if (expanded) faq.id else null
                        }
                    )
                }
                
                // Espaço extra no final
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun FAQCard(
    faq: FAQItem,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandedChange(!isExpanded) },
        colors = CardDefaults.cardColors(containerColor = SASWhite),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.pergunta,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SASGreenDark,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Recolher" else "Expandir",
                    tint = SASGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = SASLightGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))
                
                faq.resposta.forEach { passo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = passo,
                            fontSize = 14.sp,
                            color = SASGray,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
