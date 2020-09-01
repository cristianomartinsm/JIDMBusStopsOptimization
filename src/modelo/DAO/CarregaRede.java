package modelo.DAO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import controle.RedeCirculacaoViaria;
import modelo.Bairro;
import modelo.NoCirculacao;
import modelo.PontoOnibus;
import modelo.PontoOnibusNovo;
import modelo.TrechoCirculacao;

public class CarregaRede {

	public static HashMap<Integer, NoCirculacao> leNos() {
		HashMap<Integer, NoCirculacao> nosCirculacao = new HashMap<Integer, NoCirculacao>();

		try {
			Statement stmt = PostgreSQLJDBC.getConnection().createStatement();
			ResultSet rs = stmt.executeQuery("select ID_NO_CIRC from NO_CIRCULACAO;");

			NoCirculacao noCirculacao = null;
			while (rs.next()) {
				noCirculacao = new NoCirculacao(rs.getString("ID_NO_CIRC"), false);
				nosCirculacao.put(noCirculacao.getIdNoCirc(), noCirculacao);
			}
			rs.close();
			stmt.close();
			PostgreSQLJDBC.fechaConexao();

		} catch (Exception e) {
			System.err.println("Erro ao carregar nós: " + e.getMessage());
		}

		System.out.println("Nós carregados!");
		return nosCirculacao;
	}

	public static HashMap<Integer, Bairro> leBairros() {
		HashMap<Integer, Bairro> bairros = new HashMap<Integer, Bairro>();
		
		try {
			Statement stmt = PostgreSQLJDBC.getConnection().createStatement();
			ResultSet rs = stmt.executeQuery(
					"select * from BAIRRO_POPULAR;");

			Bairro bairro = null;
			double somaMetrosRuaPorPontos = 0;
			int quantBairrosValidos = 0;
			while (rs.next()) {
				bairro = new Bairro();

				bairro.setId_bairro(rs.getInt("ID_BAIRRO"));;
				bairro.setNome(rs.getString("NOME"));;
				bairro.setNum_bairro(rs.getInt("NUM_BAIRRO"));
				bairro.setNome_bairro_ibge(rs.getString("NOME_BAIRRO_IBGE"));
				bairro.setPopulacao_ibge_2010(rs.getInt("POPULACAO_IBGE_2010"));
				bairro.setComprimento_metros_rua(rs.getDouble("COMPRIMENTO_METROS_RUA"));
				bairro.setQuant_pontos_onibus(rs.getInt("QUANT_PONTOS_ONIBUS"));
				bairro.setMetros_rua_por_ponto_onibus(rs.getDouble("METROS_RUA_POR_PONTO_ONIBUS"));
				
				bairro.setQuantPontosOnibusAtual(bairro.getQuant_pontos_onibus());
				bairro.setMetrosRuaPorPontoAtual(bairro.getMetros_rua_por_ponto_onibus());
				
				bairros.put(bairro.getId_bairro(), bairro);
				
				if (bairro.getComprimento_metros_rua() > 0) {
					somaMetrosRuaPorPontos += bairro.getMetros_rua_por_ponto_onibus();
					quantBairrosValidos++;
				}
			}
			
			// Mudei de ideia: em vez da média, defini 800 metros.
			// Assim, há em média 400 metros de distância até o ponto de ônibus, caminhando para esquerda ou direita
//			Bairro.setMediaMetrosRuaPorPontoOnibus(somaMetrosRuaPorPontos/quantBairrosValidos);
			Bairro.setMediaMetrosRuaPorPontoOnibus(800);
			
			rs.close();
			stmt.close();
			PostgreSQLJDBC.fechaConexao();

		} catch (Exception e) {
			System.err.println("Erro ao carregar bairros: " + e.getMessage());
		}

		System.out.println("Bairros carregados!");		
		
		return bairros;
	}
	
	public static HashMap<Integer, TrechoCirculacao> leTrechos(RedeCirculacaoViaria redeCirculacaoViaria) {
		HashMap<Integer, TrechoCirculacao> trechosCirculacao = new HashMap<Integer, TrechoCirculacao>();

		try {
			Statement stmt = PostgreSQLJDBC.getConnection().createStatement();
			ResultSet rs = stmt.executeQuery(
					"select T.ID_TR_CIRC,"
						+ "	T.ID_TP_TRCI,"
						+ " T.ID_LOGRAD,"
						+ " T.ID_NO_INIC,"
						+ " T.ID_NO_FIN,"
						+ " ST_Length(geography(T.GEOM)) as DISTANCIA,"
						+ " B.ID_BAIRRO"
						+ " from TRECHO_CIRCULACAO T"
						+ " left join BAIRRO_POPULAR B"
						+ "		on ST_Intersects(B.GEOM, T.GEOM)"
						+ " order by T.ID_TR_CIRC;");

			TrechoCirculacao trechoCirculacao = null;
			int id_tr_circAnterior = -1;
			while (rs.next()) {
				if (id_tr_circAnterior != rs.getInt("ID_TR_CIRC")) {
					trechoCirculacao = new TrechoCirculacao();
	
					trechoCirculacao.setId_tr_circ(rs.getInt("ID_TR_CIRC"));
					trechoCirculacao.setId_tp_trci(rs.getInt("ID_TP_TRCI"));
					trechoCirculacao.setId_lograd(rs.getInt("ID_LOGRAD"));
					trechoCirculacao.setNoInicial(new NoCirculacao(rs.getString("ID_NO_INIC"), false));
					trechoCirculacao.setNoFinal(new NoCirculacao(rs.getString("ID_NO_FIN"), false));
					trechoCirculacao.setDistancia(rs.getDouble("DISTANCIA"));

					trechosCirculacao.put(trechoCirculacao.getId_tr_circ(), trechoCirculacao);
					
					//Garante (gera) que o id dos trechos vai de 1 até n para criar as restrições do Simplex
					RedeCirculacaoViaria.getTrechoIdToRestricaoIndex().put(trechoCirculacao.getId_tr_circ(), RedeCirculacaoViaria.getTrechoIdToRestricaoIndex().size() + 1);
					
					//O nó início para um trecho, é o nó final (de saída) para o nó
					NoCirculacao noChegada = RedeCirculacaoViaria.getNosCirculacao().get(trechoCirculacao.getNoFinal().getIdNoCirc());
					noChegada.getTrechosChegando().add(trechoCirculacao);
					NoCirculacao noSaida = RedeCirculacaoViaria.getNosCirculacao().get(trechoCirculacao.getNoInicial().getIdNoCirc());
					noSaida.getTrechosSaindo().add(trechoCirculacao);
				}
				//Se o trecho não fizer interseção com um bairro
				if (rs.getInt("ID_BAIRRO") != 0) {
					int id_bairro = rs.getInt("ID_BAIRRO");
					Bairro bairro = new Bairro(redeCirculacaoViaria.getBairros().get(id_bairro));
					trechoCirculacao.getBairros().add(bairro);
				}
				id_tr_circAnterior = rs.getInt("ID_TR_CIRC");
			}
			rs.close();
			stmt.close();
			PostgreSQLJDBC.fechaConexao();

		} catch (Exception e) {
			System.err.println("Erro ao carregar trechos: " + e.getMessage());
		}

		System.out.println("Trechos carregados!");
		return trechosCirculacao;
	}

	public static HashMap<Long, PontoOnibus> lePontosOnibus(RedeCirculacaoViaria redeCirculacaoViaria) {
		HashMap<Long, PontoOnibus> pontosOnibus = new HashMap<Long, PontoOnibus>();

		try {
			Statement stmt = PostgreSQLJDBC.getConnection().createStatement();
			ResultSet rs = stmt.executeQuery(
					"select ID_PONTO_ONIBUS,"
						+ "	LOGRADOURO,"
						+ " ENDERECO,"
						+ " ID_TR_CIRC,"
						+ " ST_AsText(GEOM) as GEOM"
					+ " from PONTO_ONIBUS;");

			HashSet<TrechoCirculacao> trechosComPontoOnibus = new HashSet<TrechoCirculacao>();
			while (rs.next()) {
				long idPontoOnibus = rs.getLong("ID_PONTO_ONIBUS");
				String logradouro = rs.getString("LOGRADOURO");
				String endereco = rs.getString("ENDERECO");
				int id_tr_circ = rs.getInt("ID_TR_CIRC");
				TrechoCirculacao trechoCirculacao = redeCirculacaoViaria.getTrechosCirculacao().get(id_tr_circ); 
				StringTokenizer tokenizer = new StringTokenizer(rs.getString("GEOM"), "POINT( )");
				String longitude = tokenizer.nextToken().trim();
				String latitude = tokenizer.nextToken().trim();
				String geomPonto = longitude + " " + latitude;
				
				PontoOnibus pontoOnibus = new PontoOnibus(idPontoOnibus, logradouro, endereco, trechoCirculacao, geomPonto);
				pontosOnibus.put(pontoOnibus.getIdPontoOnibus(), pontoOnibus);
				
				trechoCirculacao.getPontosOnibus().add(pontoOnibus);
				trechoCirculacao.setCaminhado(true);
				trechoCirculacao.getNoInicial().setCaminhado(true);
				trechoCirculacao.getNoFinal().setCaminhado(true);
				if (trechosComPontoOnibus.add(trechoCirculacao) == true) {
					RedeCirculacaoViaria.setDistAcumulInicialTrechosPontosOnibus(
							RedeCirculacaoViaria.getDistAcumulInicialTrechosPontosOnibus() + trechoCirculacao.getDistancia());
				}
			}
			rs.close();
			stmt.close();
			PostgreSQLJDBC.fechaConexao();

		} catch (SQLException e) {
			System.err.println("Erro ao carregar pontos de ônibus: " + e.getMessage());
		}

		System.out.println("Pontos de ônibus carregados!");
		return pontosOnibus;
	}
	
	public static HashMap<Long, PontoOnibus> lePontosOnibusArquivo() {
		String caminho = "/home/cristiano/workspace/Pontos de Ônibus Simulated Annealing/Pontos de Ônibus/stops.txt";
		File file = new File(caminho);
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(file);
		} catch (FileNotFoundException e) {
			System.out.println("Arquivo " + caminho + " não encontrado.");
		}
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String linha;
		StringTokenizer tokens;

		HashMap<Long, PontoOnibus> pontosOnibus = new HashMap<Long, PontoOnibus>();
		PontoOnibus pontoOnibus;			
		try {
			bufferedReader.readLine(); // Pula a primeira linha
			
			while ((linha = bufferedReader.readLine()) != null) {
				tokens = new StringTokenizer(linha, ",");

				long id = Long.parseLong(tokens.nextToken().trim());
				String logradouro = tokens.nextToken().trim();
				String endereco = tokens.nextToken().trim();
//				int id_tr_circ = Integer.parseInt(tokens.nextToken().trim()); //Será recuperado em outro método
				String longitude = tokens.nextToken().trim();
				String latitude = tokens.nextToken().trim();
				String geomPonto = longitude + " " + latitude;

				pontoOnibus = new PontoOnibus(id, logradouro, endereco, null, geomPonto);
				pontosOnibus.put(id, pontoOnibus);
			}
			fileReader.close();
		} catch (IOException e) {
			System.err.println("Erro de IO ao ler arquivo dos Pontos de Ônibus.");
		}

		return pontosOnibus;
	}
	
	public static void inserePontosBanco() {
		try {
			RedeCirculacaoViaria redeCirculacaoViaria = new RedeCirculacaoViaria();
			RedeCirculacaoViaria.setNosCirculacao(leNos());
			redeCirculacaoViaria.setTrechosCirculacao(leTrechos(redeCirculacaoViaria));
			
			HashMap<Long, PontoOnibus> pontosOnibus = lePontosOnibusArquivo();
			
			Connection connection = PostgreSQLJDBC.getConnection();
			
			connection = PostgreSQLJDBC.getConnection();
			
			PontoOnibus pontoOnibus;
			for (Long i : pontosOnibus.keySet()) {
				pontoOnibus = pontosOnibus.get(i);
				
				String subSql = "select TR.ID_TR_CIRC,"
						+ "ST_Distance(ST_GeomFromText('POINT(" + pontoOnibus.getGeomPonto() + ")', 4326), "
													+ "TR.GEOM) as DISTANCIA"
						+ " from TRECHO_CIRCULACAO TR order by DISTANCIA asc limit 1;";
				
				Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(subSql); //Recupera o trecho (chave estrangeira)
				rs.next();
				TrechoCirculacao trechoCirculacao = redeCirculacaoViaria.getTrechosCirculacao().get(rs.getInt("ID_TR_CIRC"));
				pontoOnibus.setTrechoCirculacao(trechoCirculacao);
				rs.close();
				
				trechoCirculacao.getPontosOnibus().add(pontoOnibus);

				String sql =  "insert into PONTO_ONIBUS (ID_PONTO_ONIBUS, LOGRADOURO, ENDERECO, ID_TR_CIRC, GEOM)"
						+ "values (?, ?, ?, ?, ST_GeomFromText('POINT(" + pontoOnibus.getGeomPonto() + ")', 4326));";
				
				PreparedStatement pstmt = connection.prepareStatement(sql);

				pstmt.setLong(1, pontoOnibus.getIdPontoOnibus());
				pstmt.setString(2, pontoOnibus.getLogradouro());
				pstmt.setString(3, pontoOnibus.getEndereco());
				pstmt.setInt(4, pontoOnibus.getTrechoCirculacao().getId_tr_circ());
				
				pstmt.executeUpdate();
				pstmt.close();
			}
			
			connection.close();
			PostgreSQLJDBC.fechaConexao();
		} catch (SQLException e) {
			System.err.println("Erro ao inserir pontos de ônibus no banco de dados: " + e.getMessage());
			e.printStackTrace();
		}
		
		System.out.println("Pontos de ônibus carregados!");
	}

	public static String padronizaNomeBairro(String nome) {
		String retorno = nome;
		String de[] = {" Primeira Seção", " Segunda Seção", " Terceira Seção", " Quarta Seção", " 1ª Seção", " 2ª Seção", " 3ª Seção", " 4ª Seção", " 5ª Seção", " 6ª Seção", " 7ª Seção"};
		String para[] = {" I", " II", " III", " IV", " I", " II", " III", " IV", " V", " VI", " VII"};
		for (int i = 0; i < de.length; i++) {
			if (retorno.endsWith(de[i])) {
				retorno = retorno.replace(de[i], para[i]);
				break;
			}
		}
		return retorno;
	}
	
	public static void inserePopulacaoBairros() {
		String caminho = "/home/cristiano/workspace/Pontos de Ônibus Simulated Annealing/População IBGE 2010/População por bairro.txt";
		File file = new File(caminho);
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(file);
		} catch (FileNotFoundException e) {
			System.out.println("Arquivo " + caminho + " não encontrado.");
		}
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String linha;
		StringTokenizer tokens;

		ArrayList<Bairro> bairros = new ArrayList<Bairro>();
		try {
			while ((linha = bufferedReader.readLine()) != null) {
				tokens = new StringTokenizer(linha);

				ArrayList<String> palavras = new ArrayList<String>();
				while (tokens.hasMoreTokens()) {
					palavras.add(tokens.nextToken());
				}
				int i = 0;
				String nome_bairro_ibge = "";
				while (i < palavras.size() - 1) {
					nome_bairro_ibge += palavras.get(i) + " ";
					i++;
				}
				nome_bairro_ibge = nome_bairro_ibge.trim();
				int populacao_ibge_2010;
				if (palavras.get(i).trim().replace(".", "").equals("-")) {
					populacao_ibge_2010 = -1;
				}
				else {
					populacao_ibge_2010 = Integer.parseInt(palavras.get(i).trim().replace(".", ""));
				}
				
				Bairro bairro = new Bairro();
				bairro.setNome_bairro_ibge(nome_bairro_ibge);
				bairro.setPopulacao_ibge_2010(populacao_ibge_2010);
				bairros.add(bairro);
			}
			fileReader.close();
		} catch (IOException e) {
			System.err.println("Erro de IO ao ler arquivo dos Pontos de Ônibus.");
		}

		
		try {
			Connection connection = PostgreSQLJDBC.getConnection();
			connection = PostgreSQLJDBC.getConnection();

			String sql =  "update BAIRRO_POPULAR set NOME_BAIRRO_IBGE = ?, POPULACAO_IBGE_2010 = ? where lower(unaccent(NOME)) = lower(unaccent(?));";
			for (Bairro bairro : bairros) {
				PreparedStatement pstmt = connection.prepareStatement(sql);
				
				pstmt.setString(1, bairro.getNome_bairro_ibge());
				pstmt.setInt(2, bairro.getPopulacao_ibge_2010());
				pstmt.setString(3, padronizaNomeBairro(bairro.getNome_bairro_ibge()));
				
				pstmt.executeUpdate();
				pstmt.close();
			}
			
			connection.close();
			PostgreSQLJDBC.fechaConexao();
		} catch (SQLException e) {
			System.err.println("Erro ao inserir população dos bairros no banco de dados: " + e.getMessage());
			e.printStackTrace();
		}
		
		System.out.println("População dos bairros inserida!");
	}

	
	public static SimpleDirectedWeightedGraph<String, TrechoCirculacao> carregaGrafo(RedeCirculacaoViaria redeCirculacaoViaria) {
		SimpleDirectedWeightedGraph<String, TrechoCirculacao> grafo = new SimpleDirectedWeightedGraph<String, TrechoCirculacao>(TrechoCirculacao.class);
		
		for (Integer i : RedeCirculacaoViaria.getNosCirculacao().keySet()) {
			grafo.addVertex(RedeCirculacaoViaria.getNosCirculacao().get(i).getIdString());
		}
				
		for (Integer i : redeCirculacaoViaria.getTrechosCirculacao().keySet()) {
			TrechoCirculacao trechoCirculacao = redeCirculacaoViaria.getTrechosCirculacao().get(i);

			String idNoInicial = trechoCirculacao.getNoInicial().getIdString();
			String idNoFinal = trechoCirculacao.getNoFinal().getIdString();
			if (idNoInicial.equals(idNoFinal) == false) { //Se for loop, dá erro
				grafo.addEdge(idNoInicial, idNoFinal, trechoCirculacao);
				grafo.setEdgeWeight(trechoCirculacao, trechoCirculacao.getDistancia());
			}
		}
		
		System.out.println("Grafo carregado!");
		
		return grafo;
	}
	
	public static SimpleDirectedWeightedGraph<String, TrechoCirculacao> carregaGrafoPontosOnibus(RedeCirculacaoViaria redeCirculacaoViaria) {
		SimpleDirectedWeightedGraph<String, TrechoCirculacao> grafo = new SimpleDirectedWeightedGraph<String, TrechoCirculacao>(TrechoCirculacao.class);

		//Para pontos de ônibus que já existiam
		TrechoCirculacao trechoCirculacao = null;
		for (Long i : RedeCirculacaoViaria.getPontosOnibusOriginais().keySet()) {
			trechoCirculacao = RedeCirculacaoViaria.getPontosOnibusOriginais().get(i).getTrechoCirculacao();
			grafo.addVertex(trechoCirculacao.getNoInicial().getIdString());
			grafo.addVertex(trechoCirculacao.getNoFinal().getIdString());

			String idNoInicial = trechoCirculacao.getNoInicial().getIdString();
			String idNoFinal = trechoCirculacao.getNoFinal().getIdString();
			if (idNoInicial.equals(idNoFinal) == false) { //Se for loop, dá erro
				grafo.addEdge(idNoInicial, idNoFinal, trechoCirculacao);
				grafo.setEdgeWeight(trechoCirculacao, trechoCirculacao.getDistancia());
			}
		}

		//Para pontos de ônibus novos
		for (PontoOnibusNovo pontoOnibusNovo : redeCirculacaoViaria.getPontosOnibusNovos()) {
			grafo.addVertex(pontoOnibusNovo.getTrechoCirculacao().getNoInicial().getIdString());
			grafo.addVertex(pontoOnibusNovo.getTrechoCirculacao().getNoFinal().getIdString());
			
			trechoCirculacao = pontoOnibusNovo.getTrechoCirculacao();
			String idNoInicial = trechoCirculacao.getNoInicial().getIdString();
			String idNoFinal = trechoCirculacao.getNoInicial().getIdString();

			if (idNoInicial.equals(idNoFinal) == false) { //Se for loop, dá erro
				grafo.addEdge(idNoInicial, idNoFinal, trechoCirculacao);
				grafo.setEdgeWeight(trechoCirculacao, trechoCirculacao.getDistancia());
			}
		}
		
		System.out.println("Grafo com pontos de ônibus carregado!");
		return grafo;
	}
	
}
