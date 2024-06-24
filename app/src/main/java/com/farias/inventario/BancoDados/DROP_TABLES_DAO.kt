package com.farias.inventario.BancoDados

import android.content.Context
import com.farias.inventario.BancoDados.DatabaseHelper.Companion.TABELA_CABECALHO_CONTAGENS
import com.farias.inventario.BancoDados.DatabaseHelper.Companion.TABELA_CONFIGURACOES
import com.farias.inventario.BancoDados.DatabaseHelper.Companion.TABELA_DIVERGENCIAS
import com.farias.inventario.BancoDados.DatabaseHelper.Companion.TABELA_ENDERECOS
import com.farias.inventario.BancoDados.DatabaseHelper.Companion.TABELA_ERROS_IMPORTACAO_PRODUTOS
import com.farias.inventario.BancoDados.DatabaseHelper.Companion.TABELA_ITENS_CONTAGENS
import com.farias.inventario.BancoDados.DatabaseHelper.Companion.TABELA_PRODUTOS
import com.farias.inventario.BancoDados.DatabaseHelper.Companion.TABELA_USUARIOS

class DROP_TABLES_DAO(context: Context) : IDROP_TABLES_DAO {

    private val escrita = DatabaseHelper(context).writableDatabase
    //private val leitura = DatabaseHelper(context).readableDatabase

    override fun apagarTodasTabelas(): Boolean {

        if (!escrita.isOpen) {
            return false
        }

        val dropConfiguracoes = """
            DROP TABLE IF EXISTS $TABELA_CONFIGURACOES;
            """.trimIndent()

        val dropProdutos = """
            DROP TABLE IF EXISTS $TABELA_PRODUTOS;
            """.trimIndent()

        val dropUsuarios = """
            DROP TABLE IF EXISTS $TABELA_USUARIOS;
            """.trimIndent()

        val dropErrosImportacao = """
            DROP TABLE IF EXISTS $TABELA_ERROS_IMPORTACAO_PRODUTOS;
            """.trimIndent()

        val dropEnderecos = """
            DROP TABLE IF EXISTS $TABELA_ENDERECOS;
            """.trimIndent()

        val dropCabecalhoContagens = """
            DROP TABLE IF EXISTS $TABELA_CABECALHO_CONTAGENS;
            """.trimIndent()

        val dropItensContagens = """
            DROP TABLE IF EXISTS $TABELA_ITENS_CONTAGENS;
            """.trimIndent()

        val dropTabelaDivergencias = """
            DROP TABLE IF EXISTS $TABELA_DIVERGENCIAS;
            """.trimIndent()

        // RECRIAR TABELAS

        val sqlTabelaConfiguracoes = """
            CREATE TABLE IF NOT EXISTS ${DatabaseHelper.TABELA_CONFIGURACOES} (
            importarCadastro BOOLEAN,
            delimitador CHAR(1),
            codigoProduto BOOLEAN,
            tamanhoCodigoProduto CHAR(2),
            codigoAuxiliar BOOLEAN,
            tamanhoCodigoAuxiliar CHAR(2),
            descricao BOOLEAN,
            senhaAcesso VARCHAR(20)
         );
         """.trimIndent()


        val sqlTabelaEnderecos = """
            CREATE TABLE IF NOT EXISTS ${TABELA_ENDERECOS} (
            id_endereco INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
            codigo_endereco NVARCHAR(20) UNIQUE,
            nome_endereco NVARCHAR(50),
            status VARCHAR(30),
            data_abertura NVARCHAR(30),
            data_fechado NVARCHAR(30)
         );
         """.trimIndent()

        val sqlTabelaUsuarios = """
            CREATE TABLE IF NOT EXISTS ${TABELA_USUARIOS} (
                id_usuario INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                codigo_usuario INTEGER NOT NULL UNIQUE,
                nome_usuario NVARCHAR(50),
                senha_usuario NVARCHAR(30),
                status NVARCHAR(30)
            );
            """.trimIndent()

        val sqlTabelaProdutos = """
             CREATE TABLE IF NOT EXISTS ${TABELA_PRODUTOS} (
                id_produto INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                codigo_produto NVARCHAR(20) UNIQUE NOT NULL,
                codigo_auxiliar_produto NVARCHAR(20),
                descricao_produto NVARCHAR(100),
                data_cadastro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
             );
             """.trimIndent()

        val sqlTabelaErrosImportacaoProdutos = """
             CREATE TABLE IF NOT EXISTS ${TABELA_ERROS_IMPORTACAO_PRODUTOS} (
                id_produto INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                linha_erro INTEGER NOT NULL,
                codigo_produto NVARCHAR(20),
                data_cadastro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
             );
             """.trimIndent()

        val sqlTabelaCabecalhoContagens = """
            
            CREATE TABLE IF NOT EXISTS ${TABELA_CABECALHO_CONTAGENS} (
                id_cabecalho INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                id_controle_cabecalho INTEGER NOT NULL,
                endereco_contagem NVARCHAR(8),
                numero_contagem CHAR(1),
                codigo_operador CHAR(6),
                codigo_acesso CHAR(3),
                hora_abertura_contagem DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                hora_fechamento_contagem DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                status_contagem NVARCHAR(10) NOT NULL DEFAULT 'ABERTO',
                identificador CHAR(1)
                );
            """.trimIndent()

        val sqlTabelaItensContagens = """
            
            CREATE TABLE IF NOT EXISTS ${TABELA_ITENS_CONTAGENS} (
                 id_item INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                 endereco_contagem NVARCHAR(8),
                 numero_contagem CHAR(1),
                 codigo_produto NVARCHAR(20) NOT NULL,
                 descricao_produto NVARCHAR(100) NOT NULL,
                 quantidade INTEGER NOT NULL,
                 hora_abertura_contagem DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                 hora_fechamento_contagem DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                 codigo_operador CHAR(6) NOT NULL,
                 idControleCabecalho INTEGER NOT NULL,
                 soma_quantidade INTEGER
                );
            """.trimIndent()

        val sqlTabelaDivergencias = """
            
            CREATE TABLE IF NOT EXISTS ${TABELA_DIVERGENCIAS} (
                id_divergencia INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                endereco_divergencia VARCHAR(8),
                codigo_produto_divergencia NVARCHAR(20) NOT NULL,
                descricao_produto_divergencia NVARCHAR(100) NOT NULL,
                quantidade1 INTEGER,
                quantidade2 INTEGER,
                quantidade3 INTEGER
        );
        """.trimIndent()

        try {
            escrita.execSQL(dropConfiguracoes)
            escrita.execSQL(dropProdutos)
            escrita.execSQL(dropUsuarios)
            escrita.execSQL(dropErrosImportacao)
            escrita.execSQL(dropEnderecos)
            escrita.execSQL(dropCabecalhoContagens)
            escrita.execSQL(dropItensContagens)
            escrita.execSQL(dropTabelaDivergencias)

            escrita.execSQL(sqlTabelaConfiguracoes)
            escrita.execSQL(sqlTabelaEnderecos)
            escrita.execSQL(sqlTabelaUsuarios)
            escrita.execSQL(sqlTabelaProdutos)
            escrita.execSQL(sqlTabelaCabecalhoContagens)
            escrita.execSQL(sqlTabelaErrosImportacaoProdutos)
            escrita.execSQL(sqlTabelaItensContagens)
            escrita.execSQL(sqlTabelaDivergencias)

        } catch (e: Exception) {
            e.message
        }
        return true
    }
}