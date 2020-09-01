package modelo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.NoSuchElementException;

import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.traverse.ClosestFirstIterator;
import org.jgrapht.traverse.RandomWalkIterator;

import controle.RedeCirculacaoViaria;

public class Solucao implements Comparable {
	
	private double aptidao;
	
	private RedeCirculacaoViaria redeCirculacaoViaria;

	private boolean aptidaoAtualizada;
	
	public Solucao() {
		this.setRedeCirculacaoViaria(new RedeCirculacaoViaria());
		
		this.setAptidao(this.calculaAptidao());
		this.setAptidaoAtualizada(true);		
	}

	public Solucao(Solucao solucao) {
		//Clona o objeto de RedeCirculacaoViaria
		this.setRedeCirculacaoViaria(new RedeCirculacaoViaria(solucao.getRedeCirculacaoViaria()));
		
		this.setAptidao(solucao.getAptidao());
		this.setAptidaoAtualizada(true);
	}
	
	public static Solucao geraVizinho(Solucao solucaoCorrente) {
		Solucao vizinho = new Solucao(solucaoCorrente);
		
		double addOuRemove = Math.random(); //SORTEIA SE ADICIONA OU REMOVE ALGUM ITEM NA MUTAÇÃO
		if (addOuRemove < 0.5) { //ADICIONA
			vizinho.adicionaNovoPontoOnibus();
		}
		else { //REMOVE
			vizinho.removeNovoPontoOnibus();
		}
		
		return vizinho;
	}

	public String evitaSelfLoops(RandomWalkIterator<String, TrechoCirculacao> rw, String noStringAtual) {
		String proxPasso = rw.next();
		
		int maxTent = 3;
		int contador = 1;
		while (proxPasso != null && proxPasso.equals(noStringAtual) && contador <= maxTent) {
			proxPasso = rw.next();
			contador++;
		}
		
		String retorno;
		if (proxPasso == null || proxPasso.equals(noStringAtual)) {
			retorno = null;
		}
		else {
			retorno = proxPasso;
		}
		return retorno;
	}
	
	public boolean confereBairro(TrechoCirculacao trechoCirculacao) {
		boolean retorno = false;
		
		for (Bairro bairro : trechoCirculacao.getBairros()) {
			if (bairro.getMetrosRuaPorPontoAtual() > Bairro.getMediaMetrosRuaPorPontoOnibus()) {
				retorno = true;
				break;
			}
		}
		
		return retorno;
	}
	
	public void adicionaNovoPontoOnibus() {
		Object nosSorteio[] = RedeCirculacaoViaria.getPontosOnibusOriginais().keySet().toArray();
		int posicaoSorteada = (int) Math.floor(Math.random() * nosSorteio.length);
		PontoOnibus pontoOnibus = RedeCirculacaoViaria.getPontosOnibusOriginais().get(nosSorteio[posicaoSorteada]);
		
		double iniOuFim = Math.random();
		NoCirculacao noSorteado = null;
		if (iniOuFim < 0.5D) {
			noSorteado = pontoOnibus.getTrechoCirculacao().getNoInicial();
		}
		else {
			noSorteado = pontoOnibus.getTrechoCirculacao().getNoFinal();
		}
		//O primeiro nó eu deixo ser caminhado, porque os pontos originais já começam sendo caminhados
//		if (noSorteado.isCaminhado()) {
//			return;
//		}
		
		Integer chaveSorteada = noSorteado.getIdNoCirc();
		
		RandomWalkIterator<String, TrechoCirculacao> rw = new RandomWalkIterator<String, TrechoCirculacao>(
				RedeCirculacaoViaria.getWeightedGraph(), noSorteado.getIdString(), false);
		
		String noStringAnterior = String.valueOf(chaveSorteada);
		NoCirculacao noAnterior = noSorteado;
		
		String noStringAtual = null;
		try {
			noStringAtual = evitaSelfLoops(rw, noStringAnterior);
		} catch (NoSuchElementException e) {
			return;
		}
		if (noStringAtual == null) { //Se o nó sorteado já for um sumidouro
			return;
		}
	
		NoCirculacao noAtual = RedeCirculacaoViaria.getNosCirculacao().get(Integer.parseInt(noStringAtual));
		
		TrechoCirculacao trechoCaminhadoAnterior = null;
		double distanciaCaminhada = 0D;
		
		Integer idStaticTrechoCaminhado = RedeCirculacaoViaria.getWeightedGraph().getEdge(noStringAnterior, noStringAtual).getId_tr_circ();
		TrechoCirculacao trechoCaminhado = getRedeCirculacaoViaria().getTrechosCirculacao().get(idStaticTrechoCaminhado);
		distanciaCaminhada = trechoCaminhado.getDistancia();
		
		//Boolean usado para esperar a avaliação do próximo trecho
		boolean olhaProxTrecho = distanciaCaminhada >= RedeCirculacaoViaria.getDISTMINPONTOS() ? true : false;
		
		HashSet<TrechoCirculacao> trechosCaminhados = new HashSet<TrechoCirculacao>();
		int contaTentativas = 1;
		String noAnteriorBackup = null;
		while (noStringAtual != null) {
			contaTentativas++;
			if (contaTentativas > 100) {
				return;
			}
			try {
				if (olhaProxTrecho == true) {
					noAnteriorBackup = noStringAnterior; //Para não perdê-lo na próxima iteração
				}
				
				noStringAnterior = noStringAtual;
				noAnterior = noAtual;
				
				noStringAtual = evitaSelfLoops(rw, noStringAnterior);
				if (noStringAtual == null) { //Se o nó sorteado já for um sumidouro
					return;
				}
				
				noAtual = RedeCirculacaoViaria.getNosCirculacao().get(Integer.parseInt(noStringAtual));
				
				trechoCaminhadoAnterior = trechoCaminhado;
				idStaticTrechoCaminhado = RedeCirculacaoViaria.getWeightedGraph().getEdge(noStringAnterior, noStringAtual).getId_tr_circ();
				trechoCaminhado = getRedeCirculacaoViaria().getTrechosCirculacao().get(idStaticTrechoCaminhado);
				
				distanciaCaminhada += trechoCaminhado.getDistancia();

				trechosCaminhados.add(trechoCaminhadoAnterior);
				trechosCaminhados.add(trechoCaminhado);

				int quantPontos = trechoCaminhado.getPontosOnibus().size();
				//Para o caso do primeiro trecho já passar o limite mínimo de distância para colocar um ponto
				int quantPontosAnterior = trechoCaminhadoAnterior.getPontosOnibus().size();

//				if (trechoCaminhado.isCaminhado() == true || trechoCaminhadoAnterior.isCaminhado() == true) { //Trecho já possui um ponto de ônibus, é melhor deixar para o próximo trecho
//					System.out.println("FUNCIONA!!");
//				}

//				if (trechoCaminhado.getNoInicial().isCaminhado() == true
//						|| trechoCaminhadoAnterior.getNoInicial().isCaminhado() == true) {
//					System.out.println("Funciona!!");
//				}
				
				if (trechoCaminhado.getNoInicial().isCaminhado() == true
						|| trechoCaminhado.getNoFinal().isCaminhado() == true
						|| trechoCaminhadoAnterior.getNoInicial().isCaminhado() == true
						|| trechoCaminhadoAnterior.getNoFinal().isCaminhado() == true
						|| trechoCaminhado.isCaminhado() == true
						|| quantPontos > 0
						|| trechoCaminhadoAnterior.isCaminhado() == true
						|| quantPontosAnterior > 0) { //Trecho já possui um ponto de ônibus, é melhor deixar para o próximo trecho
					distanciaCaminhada = 0;
					olhaProxTrecho = false;
					
					trechosCaminhados.clear();
				}
				// Trecho não tem pontos de ônibus e já está longe de outros pontos para frente e para trás
				else if (olhaProxTrecho == true && confereBairro(trechoCaminhadoAnterior) == true) { //ADICIONA O NOVO PONTO DE ÔNIBUS
					PontoOnibusNovo pontoOnibusNovo = new PontoOnibusNovo(trechoCaminhadoAnterior, (long) (getRedeCirculacaoViaria().getPontosOnibusNovos().size() + 1), trechosCaminhados);

					//O benefício é a distância caminhada até antes de olhar o próximo trecho
					pontoOnibusNovo.setBeneficioPonto(distanciaCaminhada - trechoCaminhado.getDistancia());
					trechoCaminhadoAnterior.getPontosOnibus().add(pontoOnibusNovo);
					getRedeCirculacaoViaria().getPontosOnibusNovos().add(pontoOnibusNovo);
					
					for (TrechoCirculacao trechoI : trechosCaminhados) {
						trechoI.setCaminhado(true);
						trechoI.getNoInicial().setCaminhado(true);
						trechoI.getNoFinal().setCaminhado(true);
					}
					
					for (Bairro bairro : trechoCaminhadoAnterior.getBairros()) {
						bairro.setQuantPontosOnibusAtual(bairro.getQuantPontosOnibusAtual() + 1);
						bairro.setMetrosRuaPorPontoAtual(bairro.getComprimento_metros_rua() / bairro.getQuantPontosOnibusAtual());
					}
					
					getRedeCirculacaoViaria().getGrafoPontosOnibusNovos().addVertex(noAnteriorBackup);
					getRedeCirculacaoViaria().getGrafoPontosOnibusNovos().addVertex(noStringAnterior);
					getRedeCirculacaoViaria().getGrafoPontosOnibusNovos().addEdge(noAnteriorBackup, noStringAnterior, trechoCaminhadoAnterior);

					//Igual a 1 para só somar o comprimento do trecho uma vez
					if (trechoCaminhadoAnterior.getPontosOnibus().size() == 1) {
						getRedeCirculacaoViaria().setDistAcumulTrechosPontosOnibus(
								getRedeCirculacaoViaria().getDistAcumulTrechosPontosOnibus() + trechoCaminhadoAnterior.getDistancia());
					}
					
					getRedeCirculacaoViaria().setBeneficioPontos(
							getRedeCirculacaoViaria().getBeneficioPontos() + pontoOnibusNovo.getBeneficioPonto());
					
					this.setAptidaoAtualizada(false);
	
					//INTERROMPE RANDOM WALK PORQUE JÁ CRIOU UM NOVO PONTO DE ÔNIBUS
					break;
				}
				else if (distanciaCaminhada >= RedeCirculacaoViaria.getDISTMINPONTOS()) {
					olhaProxTrecho = true;
				}
			
			} catch (NoSuchElementException e) {
				//INTERROMPE RANDOM WALK PORQUE O CAMINHAMENTO ACABOU E NÃO CRIOU UM NOVO PONTO DE ÔNIBUS
				break;
			}
		}
		
	}
	
	public void removeNovoPontoOnibus() {
		int quantPontosNovos = getRedeCirculacaoViaria().getPontosOnibusNovos().size();
		if (quantPontosNovos > 0) {
			int posicaoSorteada = (int) Math.floor(Math.random() * quantPontosNovos);
			PontoOnibusNovo pontoOnibusNovo = getRedeCirculacaoViaria().getPontosOnibusNovos().get(posicaoSorteada);
			TrechoCirculacao trechoCirculacao = pontoOnibusNovo.getTrechoCirculacao();
			trechoCirculacao.getPontosOnibus().remove(pontoOnibusNovo);
			getRedeCirculacaoViaria().getPontosOnibusNovos().remove(pontoOnibusNovo);
			
			for (Bairro bairro : trechoCirculacao.getBairros()) {
				bairro.setQuantPontosOnibusAtual(bairro.getQuantPontosOnibusAtual() - 1);
				bairro.setMetrosRuaPorPontoAtual(bairro.getComprimento_metros_rua() / bairro.getQuantPontosOnibusAtual());
			}
			
			boolean caminhado = false;
			//Informa que o trecho não é "caminhado" mais, e avalia se o noInicial precisa continuar sendo "caminhado"
			for (TrechoCirculacao trechoI : pontoOnibusNovo.getTrechosCaminhados()) {
				trechoI.setCaminhado(false);
				
				String noInicial = trechoCirculacao.getNoInicial().getIdString();
				ArrayList<String> nosFinais = (ArrayList<String>) Graphs.successorListOf(RedeCirculacaoViaria.getWeightedGraph(), noInicial);
				for (String noFinal : nosFinais) {
					TrechoCirculacao trechoAvaliado = RedeCirculacaoViaria.getWeightedGraph().getEdge(noInicial, noFinal);
					if (trechoAvaliado.isCaminhado()) { //Se pelo menos um trecho ainda está como caminhado, o noInicial deve ser caminhado
						trechoCirculacao.getNoInicial().setCaminhado(true);
						trechoCirculacao.getNoFinal().setCaminhado(true);
						caminhado = true;
						break;
					}
				}
				if (caminhado == false) {
					trechoCirculacao.getNoInicial().setCaminhado(false);
					trechoCirculacao.getNoFinal().setCaminhado(false);
				}
			}
			
			getRedeCirculacaoViaria().getGrafoPontosOnibusNovos().removeEdge(trechoCirculacao);
			
			
			if (trechoCirculacao.getPontosOnibus().size() == 0) {
				getRedeCirculacaoViaria().setDistAcumulTrechosPontosOnibus(
						getRedeCirculacaoViaria().getDistAcumulTrechosPontosOnibus() - trechoCirculacao.getDistancia());
			}
			
			getRedeCirculacaoViaria().setBeneficioPontos(
					getRedeCirculacaoViaria().getBeneficioPontos() - pontoOnibusNovo.getBeneficioPonto());
			
			// Não recalcular a aptidão após retirada quando a medida for diâmetro
			this.setAptidaoAtualizada(false);
		}
		
	}
		
	@Override
	public int compareTo(Object individuo) {
		Solucao indivComparacao = (Solucao) individuo;
		int retorno = 0;
		
		if (this.getAptidao() == indivComparacao.getAptidao()) {
			retorno = 0;
		}
		else if (this.getAptidao() > indivComparacao.getAptidao()) {
			retorno = 1;
		}
		else {
			retorno = -1;
		}
		return retorno;
	}

	public double calculaAptidao() {
		double funcaoAptidao = 0;

		// Estoura a memória antes mesmo de executar
		// Decidi evitar usar o diâmetro como função objetivo
		/*
		FloydWarshallShortestPaths<String,TrechoCirculacao> grafoDiametro = new FloydWarshallShortestPaths<String, TrechoCirculacao>(
				getRedeCirculacaoViaria().getGrafoPontosOnibusNovos());
		
		funcaoAptidao = grafoDiametro.getDiameter();
		*/
		
		// Demora muito para a função de aptidão aumentar
		//int quantOnibus = RedeCirculacaoViaria.getPontosOnibusOriginais().size() + getRedeCirculacaoViaria().getPontosOnibusNovos().size();
		//funcaoAptidao = TrechoCirculacao.getDistAcumulPontosOnibus() / quantOnibus;
		
		// Não dá para comparar direito porque os trechos tem comprimentos diferentes
		//funcaoAptidao = getRedeCirculacaoViaria().getDistAcumulPontosOnibus();
		
		funcaoAptidao = getRedeCirculacaoViaria().getBeneficioPontos();
		
		return funcaoAptidao;
	}
	
	public StringBuilder textoAptidaoSolucao(double temperaturaCorrente) {
		StringBuilder stringBuilder = new StringBuilder();
		
		stringBuilder.append(temperaturaCorrente + " " + this.getAptidao() + " " + redeCirculacaoViaria.getPontosOnibusNovos().size() + "\n");
				
		return stringBuilder;
	}
	
	public StringBuilder textoItensMelhorIndividuo() {
		StringBuilder stringBuilder = new StringBuilder();
		
		for (PontoOnibusNovo pontoOnibusNovo : this.getRedeCirculacaoViaria().getPontosOnibusNovos()) {
			stringBuilder.append(
					pontoOnibusNovo.getIdPontoNovo() + " " + pontoOnibusNovo.getTrechoCirculacao().getId_tr_circ() + "\n");
		}
		
		return stringBuilder;
	}
	
	public double getAptidao() {
		double retorno = 0D;
		if (isAptidaoAtualizada()) {
			retorno = aptidao;
		}
		else {
			 retorno = calculaAptidao();
			 this.setAptidao(retorno);
			 this.setAptidaoAtualizada(true);
		}
		return retorno;
	}
	
	public void setAptidao(double aptidaoFlavio) {
		this.aptidao = aptidaoFlavio;
	}

	public boolean isAptidaoAtualizada() {
		return aptidaoAtualizada;
	}

	public void setAptidaoAtualizada(boolean aptidaoAtualizada) {
		this.aptidaoAtualizada = aptidaoAtualizada;
	}

	public RedeCirculacaoViaria getRedeCirculacaoViaria() {
		return redeCirculacaoViaria;
	}

	public void setRedeCirculacaoViaria(RedeCirculacaoViaria redeCirculacaoViaria) {
		this.redeCirculacaoViaria = redeCirculacaoViaria;
	}
	
}
