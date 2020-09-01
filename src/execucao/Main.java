package execucao;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.SingleSourcePaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import controle.RedeCirculacaoViaria;
import modelo.Solucao;
import modelo.TrechoCirculacao;
import modelo.NoCirculacao;
import modelo.RestricaoSimplex;
import modelo.SimulatedAnnealing;

public class Main {

	public static void main(String[] args) {
		Solucao solucao = new Solucao();
		
		//CarregaRede.inserePontosBanco();
		//CarregaRede.inserePopulacaoBairros();
		
		//executa();
		
		//runClosestFirstIterator();
		//RedeCirculacaoViaria.atualizaTrechoIdRestricoesBanco();
		
		Integer idNoCircOrigem = 14625;
		Integer PASSAGEIRO_MOTORISTA_FLAG = 4;
		insereShortestPairs(idNoCircOrigem, PASSAGEIRO_MOTORISTA_FLAG);
		completaShortestPairs(idNoCircOrigem, PASSAGEIRO_MOTORISTA_FLAG);
	}

	private static void insereShortestPairs(Integer idNoCircOrigem, Integer PASSAGEIRO_MOTORISTA_FLAG) {
		Integer DISTANCIA_MINIMA = 500;
		Integer COMPRIMENTO_FAIXA = 1000;

		DijkstraShortestPath<String, TrechoCirculacao> dJShortestPaths = new DijkstraShortestPath<String, TrechoCirculacao>(RedeCirculacaoViaria.getWeightedGraph());
				
		ArrayList<NoCirculacao> nosIniciais = new ArrayList<NoCirculacao>();
		nosIniciais.add(RedeCirculacaoViaria.getNosCirculacao().get(idNoCircOrigem));
		
		ArrayList<NoCirculacao> nos = new ArrayList<NoCirculacao>(RedeCirculacaoViaria.getNosCirculacao().values());
		List<NoCirculacao> nosAleatorios = (List<NoCirculacao>) nos.clone();
		Collections.shuffle(nosAleatorios);		
		
		int contador = 0;
		for(NoCirculacao noCirc1 : nosIniciais) {
			String noString1 = String.valueOf(noCirc1.getIdNoCirc());
			SingleSourcePaths<String, TrechoCirculacao> sSPaths = dJShortestPaths.getPaths(noString1);
						
			ArrayList<Integer> faixasUtilizadas = new ArrayList<Integer>();
			for (NoCirculacao noCirc2 : nosAleatorios) {
				if (noCirc1.getIdNoCirc() != noCirc2.getIdNoCirc()) {
					String noString2 = String.valueOf(noCirc2.getIdNoCirc());
					GraphPath<String, TrechoCirculacao> path = sSPaths.getPath(noString2);
					
					if (path != null && path.getWeight() >= DISTANCIA_MINIMA) {
						Integer posicao = Math.floorDiv((int) path.getWeight(), COMPRIMENTO_FAIXA);
						
						if (faixasUtilizadas.contains(posicao) == false) {
							RedeCirculacaoViaria.insereMenorCaminhoPar(path, PASSAGEIRO_MOTORISTA_FLAG);
							faixasUtilizadas.add(posicao);
						}
					}
				}
			}
			System.out.println("Nó concluído: " + noCirc1.getIdString() + " " + contador);
			contador++;
		}
		
		System.out.println("Menor caminho calculado para todos os pares de nós!");
	}
	
	private static void completaShortestPairs(Integer idNoCircOrigem, Integer PASSAGEIRO_MOTORISTA_FLAG) {
		DijkstraShortestPath<String, TrechoCirculacao> dJShortestPaths = new DijkstraShortestPath<String, TrechoCirculacao>(RedeCirculacaoViaria.getWeightedGraph());
		ArrayList<NoCirculacao> nosACompletar = RedeCirculacaoViaria.buscaNosACompletar(idNoCircOrigem);

		int contador = 0;
		for(NoCirculacao noCirc1 : nosACompletar) {
			if (noCirc1.getIdNoCirc() == idNoCircOrigem) {
				continue;
			}
			
			String noString1 = String.valueOf(noCirc1.getIdNoCirc());
			dJShortestPaths.getPaths(noString1);
			for (NoCirculacao noCirc2 : nosACompletar) {
				if (noCirc1.getIdNoCirc() != noCirc2.getIdNoCirc()) {
					String noString2 = String.valueOf(noCirc2.getIdNoCirc());
					GraphPath<String, TrechoCirculacao> path = dJShortestPaths.getPath(noString1, noString2);
					
					if (path != null) {
						RedeCirculacaoViaria.insereMenorCaminhoPar(path, PASSAGEIRO_MOTORISTA_FLAG);
					}
				}
			}
			System.out.println("Nó concluído: " + noCirc1.getIdString() + " " + contador);
			contador++;
		}
		
		System.out.println("Menor caminho calculado para todos os pares de nós a completar!");
	}
	
	private static void runClosestFirstIterator() {
		Solucao solucao = new Solucao();
		
		ArrayList<NoCirculacao> nosCirculacao = new ArrayList<NoCirculacao>(RedeCirculacaoViaria.getNosCirculacao().values());
		Float somaTrechosProximos = 0F;
		Integer maiorNumTrechosProximos = Integer.MIN_VALUE;
		Integer menorNumTrechosProximos = Integer.MAX_VALUE;
		Integer maiorNumPontosPermitidos = Integer.MIN_VALUE;
		
		Integer metrosLimite = 800;
		
		for (Integer i = 1; i <= nosCirculacao.size(); i++) {
			//Começo com i = 1 para só gerar índices na base de dados maiores que 0 (só por estética)
			//Por isso, tenho que acessar o índice i-1
			NoCirculacao noCirculacao = nosCirculacao.get(i - 1);
			
			Set<TrechoCirculacao> trechosProximos = noCirculacao.getNextClosestVertex(metrosLimite);
			
			somaTrechosProximos += trechosProximos.size();
			if (trechosProximos.size() > maiorNumTrechosProximos) {
				maiorNumTrechosProximos = trechosProximos.size();
			}
			if (trechosProximos.size() < menorNumTrechosProximos) {
				menorNumTrechosProximos = trechosProximos.size();
			}
			
			Integer maxPontosPermitidos = 1;

			//Alguns nós tem trechos saindo mais longo que o limiar de 400m
			//Então a restrição para esse trecho é que o número de pontos seja menor ou igual que o comprimento do trecho dividido por 400
			for (TrechoCirculacao trechoCirculacao : noCirculacao.getTrechosSaindo()) {
				if (trechoCirculacao.getDistancia() > metrosLimite) {
					Double pisoDivisao = trechoCirculacao.getDistancia()/metrosLimite;
					maxPontosPermitidos += pisoDivisao.intValue();
					
					trechosProximos.add(trechoCirculacao);
				}
			}
			
			//POR ENQUANTO, NÃO VOU CONSIDERAR OS PONTOS DE ÔNIBUS JÁ EXISTENTES
			/*
			for (TrechoCirculacao trechoCirculacao : trechosProximos) {
				if (trechoCirculacao.getPontosOnibus().size() > 0) {
					maxPontosPermitidos += trechoCirculacao.getPontosOnibus().size();
				}
			}*/
			
			if (maxPontosPermitidos <= 0) {
				System.out.println("ERRO: TRECHO NÃO CONTABILIZADO PARA RESTRIÇÕES");
			}
			
			//TODO: TEM NÓS (ID: 1714, 1715, 1869, 1870, 2432, 2438, 3381...) QUE NÃO ESTÃO CONECTADOS A TRECHOS DE RUA SAINDO DO NÓ
			if (trechosProximos.size() == 0 && noCirculacao.getTrechosSaindo().size() == 0) {
				System.out.println("ERRO: TRECHO NÃO CONTABILIZADO PARA RESTRIÇÕES\nNó ID: " + noCirculacao.getIdNoCirc() + " não possui trechos próximos nem saindo dele.");
				//Pula essa iteração do for, sem registrar a restrição.
				continue;
			}

			//TODO: TEM NÓS (ID: 18305, 18591...) QUE SÓ ESTÃO CONECTADOS A UM OU MAIS PEQUENOS SELF-LOOPS
			Integer contaSelfLoops = 0;
			if (trechosProximos.size() == 0) {
				//Chega se é um self-loop: nó chegando é o mesmo nó saindo
				for (TrechoCirculacao trechoCirculacao : noCirculacao.getTrechosSaindo()) {
					if (noCirculacao.getTrechosChegando().contains(trechoCirculacao)) {
						contaSelfLoops++;
					}
				}

				if (contaSelfLoops == noCirculacao.getTrechosSaindo().size()) {
					System.out.println("ERRO: TRECHO NÃO CONTABILIZADO PARA RESTRIÇÕES\nNó ID: " + noCirculacao.getIdNoCirc() + " só possui um self-loop isolado.");
					//Pula essa iteração do for, sem registrar a restrição.
					continue;
				}
			}
			
			//Descobre o maior número de pontos de ônibus a criar só para testar
			if (maxPontosPermitidos > maiorNumPontosPermitidos) {
				maiorNumPontosPermitidos = maxPontosPermitidos;
			}
			
			RestricaoSimplex restricaoSimplex = new RestricaoSimplex(i, noCirculacao, trechosProximos, maxPontosPermitidos, RestricaoSimplex.getINEQUALDADE(), metrosLimite);
			RedeCirculacaoViaria.getRestricoesSimplex().put(i, restricaoSimplex);
		}
		
		Integer numRestricoesInequaldade = RedeCirculacaoViaria.getRestricoesSimplex().size();
		
		//Define as restrições de igualdade para os trechos que já possuem pontos de ônibus
		Integer i = numRestricoesInequaldade + 1;
		for (TrechoCirculacao trechoCirculacao : solucao.getRedeCirculacaoViaria().getTrechosCirculacao().values()) {
			if (trechoCirculacao.getPontosOnibus().size() > 0) {
				Set<TrechoCirculacao> trechoRestricao = new HashSet<TrechoCirculacao>();
				trechoRestricao.add(trechoCirculacao);
				
				Integer numPontosPermitidos = trechoCirculacao.getPontosOnibus().size();
				
				RestricaoSimplex restricaoSimplex = new RestricaoSimplex(i, null, trechoRestricao, numPontosPermitidos, RestricaoSimplex.getIGUALDADE(), metrosLimite);
				RedeCirculacaoViaria.getRestricoesSimplex().put(i, restricaoSimplex);
				i++;
			}
		}
		
		System.out.println("Número de restrições inequaldade: " + numRestricoesInequaldade);
		System.out.println("Número de restrições igualdade: " + (RedeCirculacaoViaria.getRestricoesSimplex().size() - numRestricoesInequaldade));
		System.out.println("Número de restrições totais: " + RedeCirculacaoViaria.getRestricoesSimplex().size());
		System.out.println("Média de trechos por restrição: " + somaTrechosProximos/RedeCirculacaoViaria.getRestricoesSimplex().size());
		System.out.println("Menor número de trechos por restrição: " + menorNumTrechosProximos);
		System.out.println("Maior número de trechos por restrição: " + maiorNumTrechosProximos);
		System.out.println("Maior número de pontos permitidos: " + maiorNumTrechosProximos);
	
		//Free-up Memory
		RedeCirculacaoViaria.setWeightedGraph(null);
		RedeCirculacaoViaria.atualizaRestricoesBanco(metrosLimite);
		//RedeCirculacaoViaria.atualizaMaxPontosRestricoesBanco();
	}

	public static void executa() {
		String diretorio = "/home/cristiano/Dropbox/CEFET-MG/Cristiano/Disciplinas/Computação Evolucionária/";

		float alfa = 0.9F;
		int SAMax = 1000;
		double t0 = 1000;
		double limiarMinimoTemperatura = 0.00001D;
		String caminhoSubPasta = "DeltaPositivo,T0-" + String.format("%.0f", t0) + ",SA-" + SAMax + ",alfa-"
				+ String.format("%.2f", alfa) + ",LimTemp-" + limiarMinimoTemperatura;
		// String caminhoSubPasta = "SA + AG/DeltaPositivo,T0-" +
		// String.format("%.0f", t0) + ",SA-" + SAMax + ",alfa-" +
		// String.format("%.2f", alfa) + "/";

		String caminhoPastaEscrita = diretorio + "Resultados Trabalho Final/Simulated Annealing/" + caminhoSubPasta + "/";
		File pasta = new File(caminhoPastaEscrita);
		if (pasta.exists() == false) {
			pasta.mkdirs();
		}

		String caminhoPadraoEscrita = caminhoPastaEscrita + "resultado.txt";
		String caminhoEscritaResultados = caminhoPadraoEscrita.replace(".txt", "_1.txt");
		// JÁ COMEÇA COM O NÚMERO 1 NO NOME
		File file = new File(caminhoEscritaResultados);
		int contaExecucoes = 1;
		int maximoExecucoes = 33;

		for (contaExecucoes = 1; contaExecucoes <= maximoExecucoes; contaExecucoes++) {
			int contadorNovoNome = 1;
			while (file.exists() == true) {
				caminhoEscritaResultados = caminhoPadraoEscrita.replace(".txt", "_" + contadorNovoNome + ".txt");
				file = new File(caminhoEscritaResultados);
				contadorNovoNome++;
			}

			Solucao solucaoCorrente = new Solucao();
			SimulatedAnnealing sa = new SimulatedAnnealing(alfa, SAMax, t0, limiarMinimoTemperatura, solucaoCorrente);

			Solucao melhorSolucao = sa.otimiza();
			escreveResultados(caminhoEscritaResultados, sa.getResultadosTemperaturas().toString());

			String itensMelhorIndividuo = melhorSolucao.textoItensMelhorIndividuo().toString();
			escreveResultados(caminhoEscritaResultados, itensMelhorIndividuo);
		}

	}

	private static void escreveResultados(String caminho, String resultadosGeracaoAtual) {
		File file = new File(caminho);

		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;

		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Erro de IO ao criar arquivo de resultados.");
		}

		try {
			fileWriter = new FileWriter(file, true); // TRUE PARA PERMITIR O
														// APPEND
			bufferedWriter = new BufferedWriter(fileWriter);

			bufferedWriter.write(resultadosGeracaoAtual);
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Erro de IO ao escrever resultados.");
		}
	}

}
