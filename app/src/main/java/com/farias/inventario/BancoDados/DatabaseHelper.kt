package com.farias.inventario.BancoDados

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.sql.Date
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, NOME_BANCO_DADOS, null, VERSAO_BANCO_DADOS) {

    companion object {
        const val NOME_BANCO_DADOS = "inventario.db"
        const val VERSAO_BANCO_DADOS = 2
        const val TABELA_ENDERECOS = "enderecos"
        const val TABELA_USUARIOS = "usuarios"
        const val TABELA_PRODUTOS = "produtos"
        const val TABELA_ERROS_IMPORTACAO_PRODUTOS = "erros_importacao_produtos"
        const val TABELA_CABECALHO_CONTAGENS = "cabecalho_contagens"
        const val TABELA_ITENS_CONTAGENS = "itens_contagens"
        const val TABELA_DIVERGENCIAS = "divergencias"
        const val TABELA_CONFIGURACOES = "configuracoes"

        @Volatile
        private var instance: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            if (instance == null) {
                synchronized(DatabaseHelper::class.java) {
                    if (instance == null) {
                        instance = DatabaseHelper(context)
                    }
                }
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {

        val sqlTabelaConfiguracoes = """
            CREATE TABLE IF NOT EXISTS ${TABELA_CONFIGURACOES} (
            importarCadastro BOOLEAN,
            delimitador CHAR(1),
            codigoProduto BOOLEAN,
            tamanhoCodigoProduto INTEGER (2),
            codigoAuxiliar BOOLEAN, 
            tamanhoCodigoAuxiliar INTEGER (2),
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

            db?.execSQL(sqlTabelaConfiguracoes)
            db?.execSQL(sqlTabelaEnderecos)
            db?.execSQL(sqlTabelaUsuarios)
            db?.execSQL(sqlTabelaProdutos)
            db?.execSQL(sqlTabelaCabecalhoContagens)
            db?.execSQL(sqlTabelaErrosImportacaoProdutos)
            db?.execSQL(sqlTabelaItensContagens)
            db?.execSQL(sqlTabelaDivergencias)

            Log.i("CRIACAO_TABELAS", "Sucesso ao criar tabelas:")
        } catch (e: Exception) {
            Log.i("CRIACAO_TABELAS", "Erro ao criar tabelas: ${e.message.toString()} ")
        }
    }


    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
      /*  Log.i("BancoDeDados", "onUpgrade chamado: oldVersion=$oldVersion, newVersion=$newVersion")

        if (oldVersion < 2) {
            val alterTable = "ALTER TABLE $TABELA_CONFIGURACOES ADD COLUMN senha VARCHAR(20)"
            try {
                db?.execSQL(alterTable)
                Log.i("BancoDeDados", "Coluna 'senha' adicionada com sucesso na tabela $TABELA_CONFIGURACOES")
            } catch (e: Exception) {
                Log.e("BancoDeDados", "Erro ao adicionar coluna 'senha': ${e.message}")
            }
        }

       */
    }

    fun gerarBackup(context: Context, uri: Uri): Boolean {
        return try {
            val inputStream = FileInputStream(context.getDatabasePath(NOME_BANCO_DADOS))
            val outputStream = context.contentResolver.openOutputStream(uri)

            if (outputStream != null) {
                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.i("ROLA_BACKUP", "gerarBackup: ${e.message}")
            false
        }
    }



     fun restaurarBackup(context: Context, uri: Uri): Boolean {
        val bancoDados = context.getDatabasePath(NOME_BANCO_DADOS)
        val databaseHelper = DatabaseHelper(context)

        return try {
            // Fechar o banco de dados atual
            databaseHelper.close()

            // Copiar o arquivo de backup para o local do banco de dados
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(bancoDados).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            // Reabrir o banco de dados
            databaseHelper.writableDatabase

            true
        } catch (e: Exception) {
            Log.e("ROLA_BACKUP", "restaurarBackup: ${e.message}")
            false
        }
    }

}