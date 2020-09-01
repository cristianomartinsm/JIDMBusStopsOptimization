package modelo.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import controle.RedeCirculacaoViaria;
import modelo.NoCirculacao;
import modelo.RestricaoSimplex;
import modelo.TrechoCirculacao;

public class PersistenciaRestricoes {

	public static void deletaRestricoes(Integer metrosLimite) {
		try {
			Statement stmt = PostgreSQLJDBC.getConnection().createStatement();
			stmt.executeUpdate("delete from RESTRICOES_SIMPLEX where metros_limite = " + metrosLimite);
			stmt.close();
			PostgreSQLJDBC.fechaConexao();

		} catch (Exception e) {
			System.err.println("Erro ao deletar restrições: " + e.getMessage());
		}

		System.out.println("Restrições antigas excluídas!");
	}
	
	public static void insereRestricoes(HashMap<Integer,RestricaoSimplex> restricoes) {
		Connection connection = PostgreSQLJDBC.getConnection();
		
		try {
			for (RestricaoSimplex restricao : restricoes.values()) {
				StringBuilder sb = new StringBuilder("insert into RESTRICOES_SIMPLEX (id, id_tr_circ, max_pontos_permitidos, igualdade_inequaldade, id_no_circ, metros_limite) values");
				
				Integer contaInterrogacoes = 0;
				int quant = restricao.getTrechosProximos().size();
				if (quant == 0) {
					System.out.println("ERRO: TRECHO NÃO CONTABILIZADO PARA RESTRIÇÕES");
				}
				for (int i = 0; i < quant; i++) {
					sb.append(" (?, ?, ?, ?, ?, ?),");
					contaInterrogacoes += 3;
				}
				//Remove a última vírgula para colocar um ponto e vírgula
				sb.setLength(sb.length() - 1);
				sb.append(";");
				
				PreparedStatement pstmt = connection.prepareStatement(sb.toString());

				Integer contador = 1;
				for (TrechoCirculacao trecho : restricao.getTrechosProximos()) {
					pstmt.setInt(contador, restricao.getId());
					contador++;
					pstmt.setInt(contador, trecho.getId_tr_circ());
					contador++;
					pstmt.setInt(contador, restricao.getMaxPontosPermitidos());
					contador++;
					pstmt.setInt(contador, restricao.getIgualdadeInequaldade());
					contador++;
					if (restricao.getNoGerador() != null) {
						pstmt.setInt(contador, restricao.getNoGerador().getIdNoCirc());
					}
					else {
						pstmt.setNull(contador, Types.INTEGER);
					}
					contador++;
					pstmt.setInt(contador, restricao.getMetrosLimite());
					contador++;
				}
				
				pstmt.executeUpdate();
			}
						
			PostgreSQLJDBC.fechaConexao();

		} catch (Exception e) {
			System.err.println("Erro ao inserir restrições: " + e.getMessage());
		}

		System.out.println("Base das restrições atualizada!");
	}

	public static void deletaTrechoIdRestricoes() {
		try {
			Statement stmt = PostgreSQLJDBC.getConnection().createStatement();
			stmt.executeUpdate("delete from TRECHO_ID_TO_RESTRICOES");
			stmt.close();
			PostgreSQLJDBC.fechaConexao();

		} catch (Exception e) {
			System.err.println("Erro ao deletar Trecho Id Restrições: " + e.getMessage());
		}

		System.out.println("Trecho Id Restrições antigos excluídos!");		
	}

	public static void insereTrechoIdRestricoes(HashMap<Integer, Integer> trechoIdToRestricaoIndex) {
		Connection connection = PostgreSQLJDBC.getConnection();
		
		try {
			for (Integer trechoId : trechoIdToRestricaoIndex.keySet()) {
				StringBuilder sb = new StringBuilder("insert into TRECHO_ID_TO_RESTRICOES (id_tr_circ, id_restricao) values (?, ?);");
				
				PreparedStatement pstmt = connection.prepareStatement(sb.toString());
				
				pstmt.setInt(1, trechoId);
				pstmt.setInt(2, trechoIdToRestricaoIndex.get(trechoId));

				pstmt.executeUpdate();
			}						

			PostgreSQLJDBC.fechaConexao();
		} catch (Exception e) {
			System.err.println("Erro ao inserir Trecho Id Restrições: " + e.getMessage());
		}

		System.out.println("Base dos Trecho Id Restrições atualizada!");		
	}

	public static void atualizaMaxPontosRestricoesBanco() {
		HashMap<Integer, Integer> idToMaxPontos = new HashMap<Integer, Integer>();
		
		Connection connection = PostgreSQLJDBC.getConnection();		
		
		try {
			String query = "select id, max_pontos_permitidos from restricoes_simplex where igualdade_inequaldade = 1 order by id";						
			
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			
			while(rs.next()) {
				idToMaxPontos.put(rs.getInt("id"), rs.getInt("max_pontos_permitidos"));
			}
		} catch (Exception e) {
			System.err.println("Erro ao buscar Restrições: " + e.getMessage());
		}
		
		for (Integer id : idToMaxPontos.keySet()) {
			Integer maxPontosPermitidos = RedeCirculacaoViaria.getRestricoesSimplex().get(id).getMaxPontosPermitidos();
			try {
				String query = "update restricoes_simplex set max_pontos_permitidos = " + maxPontosPermitidos + " where id = " + id;						
				connection.createStatement().executeUpdate(query);
			} catch (Exception e) {
				System.err.println("Erro ao atualizar Restrição: " + e.getMessage());
			}
		}

		PostgreSQLJDBC.fechaConexao();
		System.out.println("Base dos Máximo de Pontos Permitidos por Restrição atualizada!");		
	}

	public static void insereMenorCaminhoPar(Long noInic, Long noFin, List<TrechoCirculacao> edgesList) {
		Connection connection = PostgreSQLJDBC.getConnection();
		
		try {
			StringBuilder sb = new StringBuilder("insert into MENOR_CAMINHO_PAR_NO_CIRC (id_no_inic, id_no_fin, id_tr_circ, ordem_caminho) values ");
			for (TrechoCirculacao trechoCirculacao : edgesList) {
				sb.append("(?, ?, ?, ?), ");
			}
			//Remove a última vírgula e espaço para colocar um ponto e vírgula
			sb.setLength(sb.length() - 2);
			sb.append(";");
			
			int ordemCaminho = 1;
			int contador = 1;
			
			PreparedStatement pstmt = connection.prepareStatement(sb.toString());
			for (TrechoCirculacao trechoCirculacao : edgesList) {				
				pstmt.setLong(contador, noInic);
				contador++;
				pstmt.setLong(contador, noFin);
				contador++;
				pstmt.setInt(contador, trechoCirculacao.getId_tr_circ());
				contador++;
				pstmt.setInt(contador, ordemCaminho);
				contador++;
				ordemCaminho++;
			}					

			pstmt.executeUpdate();

		} catch (Exception e) {
			System.err.println("Erro ao inserir caminho: " + e.getMessage());
		}
	}
	
	public static void insereDistanciaMenorCaminhoPar(Long noInic, Long noFin, Double pesoTotal, Integer passageiroMotoristaFlag) {
		Connection connection = PostgreSQLJDBC.getConnection();
		
		try {
			StringBuilder sb = new StringBuilder("insert into DISTANCIA_MENOR_CAMINHO_PAR_NO_CIRC (id_no_inic, id_no_fin, distancia, passageiro_motorista_flag) values (?, ?, ?, ?);");
			
			PreparedStatement pstmt = connection.prepareStatement(sb.toString());
			
			pstmt.setLong(1, noInic);
			pstmt.setLong(2, noFin);
			pstmt.setDouble(3, pesoTotal);
			pstmt.setInt(4, passageiroMotoristaFlag);
			
			pstmt.executeUpdate();
	
			PostgreSQLJDBC.fechaConexao();

		} catch (Exception e) {
			System.err.println("Erro ao inserir caminho: " + e.getMessage());
		}
	}
	
	public static ArrayList<NoCirculacao> buscaNosACompletar(Integer idNoCircOrigem) {
		ArrayList<NoCirculacao> nosCirculacao = new ArrayList<NoCirculacao>();
		Connection connection = PostgreSQLJDBC.getConnection();
		
		try {
			String sql = "select id_no_fin from distancia_menor_caminho_par_no_circ where id_no_inic = " + idNoCircOrigem + ";";
			
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			
			NoCirculacao noCirculacao = new NoCirculacao(idNoCircOrigem);
			nosCirculacao.add(noCirculacao);
			
			while(rs.next()) {
				noCirculacao = new NoCirculacao(rs.getInt("id_no_fin"));
				nosCirculacao.add(noCirculacao);
			}
			
			PostgreSQLJDBC.fechaConexao();

		} catch (Exception e) {
			System.err.println("Erro ao buscar nós: " + e.getMessage());
		}
		
		return nosCirculacao;
	}
	
}
