package controle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.jgrapht.GraphPath;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import modelo.Bairro;
import modelo.NoCirculacao;
import modelo.PontoOnibus;
import modelo.PontoOnibusNovo;
import modelo.RestricaoSimplex;
import modelo.Solucao;
import modelo.TrechoCirculacao;
import modelo.DAO.CarregaRede;
import modelo.DAO.PersistenciaRestricoes;

public class RedeCirculacaoViaria {
	
	private static HashMap<Integer, NoCirculacao> nosCirculacao;
	private HashMap<Integer, TrechoCirculacao> trechosCirculacao; // Não é static porque a instância varia para cada solução vizinha
	private static HashMap<Long, PontoOnibus> pontosOnibusOriginais;
	private ArrayList<PontoOnibusNovo> pontosOnibusNovos; // Não é static porque a instância varia para cada solução vizinha
//	private static HashMap<Integer, Bairro> bairrosInicial;
	private HashMap<Integer, Bairro> bairros;
	private static SimpleDirectedWeightedGraph<String, TrechoCirculacao> weightedGraph;
	private SimpleDirectedWeightedGraph<String, TrechoCirculacao> grafoPontosOnibusNovos; // Não é static porque a instância varia para cada solução vizinha
	
	private static HashMap<Integer, Integer> trechoIdToRestricaoIndex = new HashMap<Integer, Integer>();
	private static HashMap<Integer, RestricaoSimplex> restricoesSimplex = new HashMap<Integer, RestricaoSimplex>();
	
	//Deixo armazenado um grafo dos pontos de ônibus para agilizar a criação de uma solução
//	private static SimpleDirectedWeightedGraph<String, TrechoCirculacao> grafoInicialPontosOnibusNovos;
	//O mesmo para os trechos
//	private static HashMap<Integer, TrechoCirculacao> trechosCirculacaoInicial;
	
	//Quando a função aptidão é o comprimento dos trechos
	private static double distAcumulInicialTrechosPontosOnibus;
	private double distAcumulTrechosPontosOnibus;
	
	//Quando a função aptidão é a distância a caminhar até o trecho do ônibus
	private double beneficioPontos;
	
	private static final double DISTMINPONTOS = 400;
	
	public RedeCirculacaoViaria() {
		//Coleções static não precisam ser carregados novamente
//		if (getBairrosInicial() == null) {
//			setBairrosInicial(CarregaRede.leBairros());
//		}
		setBairros(CarregaRede.leBairros());
		
//		if (getNosCirculacao() == null) {
//			setNosCirculacao(CarregaRede.leNos());
//		}
		setNosCirculacao(CarregaRede.leNos());
		
//		if (getTrechosCirculacaoInicial() == null) {
//			setTrechosCirculacaoInicial(CarregaRede.leTrechos());
//		}
		setTrechosCirculacao(CarregaRede.leTrechos(this));
		
//		if (getPontosOnibusOriginais() == null) {
//			setPontosOnibusOriginais(CarregaRede.lePontosOnibus(this));
//		}
		setPontosOnibusOriginais(CarregaRede.lePontosOnibus(this));
		setDistAcumulTrechosPontosOnibus(getDistAcumulInicialTrechosPontosOnibus());
		
		setPontosOnibusNovos(new ArrayList<PontoOnibusNovo>());
		setBeneficioPontos(0D);
		
//		if (getWeightedGraph() == null) {
//			setWeightedGraph(CarregaRede.carregaGrafo(this));
//		}
		setWeightedGraph(CarregaRede.carregaGrafo(this));
		
//		if (getGrafoInicialPontosOnibusNovos() == null) {
//			setGrafoInicialPontosOnibusNovos(CarregaRede.carregaGrafoPontosOnibus(this));
//		}
		//Utiliza a versão carregada na linha anterior. Agiliza a criação a partir da segunda solução
		setGrafoPontosOnibusNovos(CarregaRede.carregaGrafoPontosOnibus(this));
	}

	public RedeCirculacaoViaria(RedeCirculacaoViaria redeCirculacaoViaria) {
		//Clona atributos não static
		HashMap<Integer, TrechoCirculacao> cloneTrechos = new HashMap<Integer, TrechoCirculacao>();
		for (Integer i : redeCirculacaoViaria.getTrechosCirculacao().keySet()) {
			TrechoCirculacao trechoCirculacao = new TrechoCirculacao(redeCirculacaoViaria.getTrechosCirculacao().get(i));
			cloneTrechos.put(i, trechoCirculacao);
		}
		setTrechosCirculacao(cloneTrechos);

		ArrayList<PontoOnibusNovo> clonePontosOnibus = new ArrayList<PontoOnibusNovo>();
		SimpleDirectedWeightedGraph<String, TrechoCirculacao> cloneGrafoPontosOnibusNovos= new SimpleDirectedWeightedGraph<String, TrechoCirculacao>(TrechoCirculacao.class);

		PontoOnibusNovo novoPontoOnibusNovo = null;
		for (PontoOnibusNovo forPontoOnibusNovo : redeCirculacaoViaria.getPontosOnibusNovos()) {
			TrechoCirculacao trechoCirculacao = getTrechosCirculacao().get(forPontoOnibusNovo.getTrechoCirculacao().getId_tr_circ());
			
			for (PontoOnibus forInTrechoPontoOnibus : trechoCirculacao.getPontosOnibus()) {
				if (forInTrechoPontoOnibus instanceof PontoOnibusNovo) {
					novoPontoOnibusNovo = (PontoOnibusNovo) forInTrechoPontoOnibus;

					HashSet<TrechoCirculacao> trechosCaminhadosClone = new HashSet<TrechoCirculacao>();
					for (TrechoCirculacao trechoI : novoPontoOnibusNovo.getTrechosCaminhados()) {
						trechosCaminhadosClone.add(getTrechosCirculacao().get(trechoI.getId_tr_circ()));
					}
					novoPontoOnibusNovo.setTrechosCaminhados(trechosCaminhadosClone);
					
					break;
				}
			}
			
			clonePontosOnibus.add(novoPontoOnibusNovo);	

			String verticeInicial = trechoCirculacao.getNoInicial().getIdString();
			String verticeFinal = trechoCirculacao.getNoFinal().getIdString();
			cloneGrafoPontosOnibusNovos.addVertex(verticeInicial);
			cloneGrafoPontosOnibusNovos.addVertex(verticeFinal);
			cloneGrafoPontosOnibusNovos.addEdge(
					verticeInicial,
					verticeFinal,
					trechoCirculacao);
			cloneGrafoPontosOnibusNovos.setEdgeWeight(trechoCirculacao, trechoCirculacao.getDistancia());
		}
		setPontosOnibusNovos(clonePontosOnibus);		
		setGrafoPontosOnibusNovos(cloneGrafoPontosOnibusNovos);
		
		setDistAcumulTrechosPontosOnibus(redeCirculacaoViaria.getDistAcumulTrechosPontosOnibus());
		setBeneficioPontos(redeCirculacaoViaria.getBeneficioPontos());
	}
	
	public static HashMap<Integer, NoCirculacao> getNosCirculacao() {
		return nosCirculacao;
	}

	public static void setNosCirculacao(HashMap<Integer, NoCirculacao> nosCirculacao) {
		RedeCirculacaoViaria.nosCirculacao = nosCirculacao;
	}

	public HashMap<Integer, TrechoCirculacao> getTrechosCirculacao() {
		return trechosCirculacao;
	}

	public void setTrechosCirculacao(HashMap<Integer, TrechoCirculacao> trechosCirculacao) {
		this.trechosCirculacao = trechosCirculacao;
	}

	public static HashMap<Long, PontoOnibus> getPontosOnibusOriginais() {
		return pontosOnibusOriginais;
	}

	public static void setPontosOnibusOriginais(HashMap<Long, PontoOnibus> pontosOnibusOriginais) {
		RedeCirculacaoViaria.pontosOnibusOriginais = pontosOnibusOriginais;
	}

	public ArrayList<PontoOnibusNovo> getPontosOnibusNovos() {
		return pontosOnibusNovos;
	}

	public void setPontosOnibusNovos(ArrayList<PontoOnibusNovo> pontosOnibusNovos) {
		this.pontosOnibusNovos = pontosOnibusNovos;
	}

	public static SimpleDirectedWeightedGraph<String, TrechoCirculacao> getWeightedGraph() {
		return weightedGraph;
	}

	public static void setWeightedGraph(SimpleDirectedWeightedGraph<String, TrechoCirculacao> weightedGraph) {
		RedeCirculacaoViaria.weightedGraph = weightedGraph;
	}

	public SimpleDirectedWeightedGraph<String, TrechoCirculacao> getGrafoPontosOnibusNovos() {
		return grafoPontosOnibusNovos;
	}

	public void setGrafoPontosOnibusNovos(SimpleDirectedWeightedGraph<String, TrechoCirculacao> grafoPontosOnibusNovos) {
		this.grafoPontosOnibusNovos = grafoPontosOnibusNovos;
	}

	public HashMap<Integer, Bairro> getBairros() {
		return bairros;
	}

	public void setBairros(HashMap<Integer, Bairro> bairros) {
		this.bairros = bairros;
	}

	public static double getDISTMINPONTOS() {
		return DISTMINPONTOS;
	}
	
	public static double getDistAcumulInicialTrechosPontosOnibus() {
		return distAcumulInicialTrechosPontosOnibus;
	}

	public static void setDistAcumulInicialTrechosPontosOnibus(double distAcumulInicialTrechosPontosOnibus) {
		RedeCirculacaoViaria.distAcumulInicialTrechosPontosOnibus = distAcumulInicialTrechosPontosOnibus;
	}

	public double getDistAcumulTrechosPontosOnibus() {
		return distAcumulTrechosPontosOnibus;
	}

	public void setDistAcumulTrechosPontosOnibus(double distAcumulTrechosPontosOnibus) {
		this.distAcumulTrechosPontosOnibus = distAcumulTrechosPontosOnibus;
	}

	public double getBeneficioPontos() {
		return beneficioPontos;
	}

	public void setBeneficioPontos(double beneficioPontos) {
		this.beneficioPontos = beneficioPontos;
	}

	public static HashMap<Integer, Integer> getTrechoIdToRestricaoIndex() {
		return RedeCirculacaoViaria.trechoIdToRestricaoIndex;
	}

	public static void setTrechoIdToRestricaoIndex(HashMap<Integer, Integer> trechoIdToRestricaoIndex) {
		RedeCirculacaoViaria.trechoIdToRestricaoIndex = trechoIdToRestricaoIndex;
	}

	public static HashMap<Integer, RestricaoSimplex> getRestricoesSimplex() {
		return restricoesSimplex;
	}

	public static void setRestricoesSimplex(HashMap<Integer, RestricaoSimplex> restricoesSimplex) {
		RedeCirculacaoViaria.restricoesSimplex = restricoesSimplex;
	}

	public static void removeRestricoesDuplicadas() {
		System.out.println("Iniciou com " + getRestricoesSimplex().size() + " restrições.");
		
		ArrayList<RestricaoSimplex> restricoesASeremRemovidas = new ArrayList<RestricaoSimplex>();
		
		int contadorRestricoesPrincipais = 0;
		//Para todas as restrições de inequalidade que não são duplicadas (não estão para serem removidas)
		for (RestricaoSimplex restricaoSimplex : getRestricoesSimplex().values()) {
			if (restricaoSimplex.getIgualdadeInequaldade() == RestricaoSimplex.getINEQUALDADE() &&
				restricoesASeremRemovidas.contains(restricaoSimplex) == false) {
				
				//Contra todas as outras restrições de inequalidade que ainda não foram removidas
				for (RestricaoSimplex restricaoSimplex2 : getRestricoesSimplex().values()) {
					if (restricaoSimplex2.getIgualdadeInequaldade() == RestricaoSimplex.getINEQUALDADE() &&
						restricaoSimplex != restricaoSimplex2 &&
						restricoesASeremRemovidas.contains(restricaoSimplex2) == false) {
						
						//Se uma restrição conter todas as variáveis de outra restrição, essa outra restrição deve ser removida
						if (restricaoSimplex.getTrechosProximos().containsAll(restricaoSimplex2.getTrechosProximos())) {
							restricoesASeremRemovidas.add(restricaoSimplex2);
							
							//Imprime só quando for múltiplo de 1.000 para evitar muitos prints
							if (restricoesASeremRemovidas.size() % 1000 == 0) {
								System.out.println("Total de " + restricoesASeremRemovidas.size() + " restrições removidas! Restrição " + restricaoSimplex.getId() + " removeu a restrição " + restricaoSimplex2.getId());
							}
						}
					}
				}
			}
			contadorRestricoesPrincipais++;
			//Imprime só quando for múltiplo de 10.000 para evitar muitos prints
			if (contadorRestricoesPrincipais % 10000 == 0) {
				System.out.println("Restrição atual: " + contadorRestricoesPrincipais);
			}
		}
		
		//Remove as restrições repetidas
		for (RestricaoSimplex restricaoSimplex : restricoesASeremRemovidas) {
			getRestricoesSimplex().remove(restricaoSimplex.getId());
		}
		
		System.out.println("Terminou com " + getRestricoesSimplex().size() + " restrições.");
	}

	public static void atualizaMaxPontosRestricoesBanco() {
		PersistenciaRestricoes.atualizaMaxPontosRestricoesBanco();
	}
	
	public static void atualizaRestricoesBanco(Integer metrosLimite) {
		removeRestricoesDuplicadas();
		PersistenciaRestricoes.deletaRestricoes(metrosLimite);
		PersistenciaRestricoes.insereRestricoes(getRestricoesSimplex());
	}

	public static void atualizaTrechoIdRestricoesBanco() {
		Solucao solucao = new Solucao();
		
		PersistenciaRestricoes.deletaTrechoIdRestricoes();
		PersistenciaRestricoes.insereTrechoIdRestricoes(getTrechoIdToRestricaoIndex());
	}
	
	public static void insereMenorCaminhoPar(GraphPath<String, TrechoCirculacao> path, Integer passageiroMotoristaFlag) {
		Long noInic = Long.parseLong(path.getStartVertex());
		Long noFin = Long.parseLong(path.getEndVertex());
		
		PersistenciaRestricoes.insereMenorCaminhoPar(noInic, noFin, path.getEdgeList());
		PersistenciaRestricoes.insereDistanciaMenorCaminhoPar(noInic, noFin, path.getWeight(), passageiroMotoristaFlag);
	}
	
	public static ArrayList<NoCirculacao> buscaNosACompletar(Integer idNoCircOrigem) {
		return PersistenciaRestricoes.buscaNosACompletar(idNoCircOrigem);
	}
	
}