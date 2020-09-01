package modelo;

import java.text.DecimalFormat;

import controle.RedeCirculacaoViaria;

public class SimulatedAnnealing {

	private float alfa;
	private int SAMax;
	private double t0;
	private double limiarMinimoTemperatura;
	private Solucao solucaoCorrente;
	
	private StringBuilder resultadosTemperaturas;
	
	public SimulatedAnnealing(float alfa, int SAMax, double t0, double limiarMinimoTemperatura, Solucao solucaoCorrente) {
		this.setAlfa(alfa);
		this.setSAMax(SAMax);
		this.setT0(t0);
		this.setLimiarMinimoTemperatura(limiarMinimoTemperatura);
		this.setSolucaoCorrente(solucaoCorrente);
		this.setResultadosTemperaturas(new StringBuilder());
	}
	
	public Solucao otimiza() {
		DecimalFormat dfDouble6 = new DecimalFormat();
		dfDouble6.setMaximumFractionDigits(6);
		DecimalFormat dfDouble = new DecimalFormat();
		dfDouble.setMaximumFractionDigits(4);
		DecimalFormat dfInt = new DecimalFormat();
		dfInt.setMaximumFractionDigits(0);
		
		Solucao melhorSolucao = new Solucao(solucaoCorrente); //Melhor solução atual
		double aptidaoInicial = melhorSolucao.getAptidao();
		int iterT = 0; //Número de iterações na temperatura T
		double temperaturaCorrente = t0; //Temperatura corrente
		
		while (temperaturaCorrente > limiarMinimoTemperatura) {
			setResultadosTemperaturas(this.getResultadosTemperaturas()
				.append(melhorSolucao.textoAptidaoSolucao(temperaturaCorrente)));
			
			while (iterT < SAMax) {
				iterT++;
				
				Solucao vizinho = Solucao.geraVizinho(solucaoCorrente);
				double delta = vizinho.getAptidao() - solucaoCorrente.getAptidao();
				if (delta > 0) {
					//Não precisa criar uma nova instância de solução
					//solucaoCorrente = new Solucao(vizinho);
					solucaoCorrente = vizinho;
					if (solucaoCorrente.getAptidao() > melhorSolucao.getAptidao()) {
						melhorSolucao = new Solucao(solucaoCorrente);
					}
				}
				else {
					double xSorteado = Math.random();
					if (xSorteado < Math.exp((delta)/temperaturaCorrente)) {					
//					if (xSorteado < Math.exp((-1 * delta)/temperaturaCorrente)) {
						//Não precisa criar uma nova instância de solução
						//solucaoCorrente = new Solucao(vizinho);
						solucaoCorrente = vizinho;
					}
				}
			} //fim enquanto iterT
			temperaturaCorrente = temperaturaCorrente * alfa;
			iterT = 0;
			System.out.println(//"Dist.Min.: " + dfInt.format(RedeCirculacaoViaria.getDISTMINPONTOS())
				//+ "\tAp.Ini.: " + dfDouble.format(aptidaoInicial)
				"T0: " + dfInt.format(getT0())
				+ "\tSAMax: " + dfInt.format(getSAMax())
				+ "\tLim.Temp. " + dfDouble6.format(getLimiarMinimoTemperatura())
				+ "\tMelhr.Apt.: " + dfDouble.format(melhorSolucao.getAptidao())
				+ "\tQuant.Pont.: " + dfInt.format(melhorSolucao.getRedeCirculacaoViaria().getPontosOnibusNovos().size())
				+ "\tTemp.Corrnt: " + dfDouble.format(temperaturaCorrente));
		} //fim enquanto temperaturaCorrente

		setResultadosTemperaturas(this.getResultadosTemperaturas()
				.append(melhorSolucao.textoAptidaoSolucao(temperaturaCorrente)));
		
		return melhorSolucao;
	}
	
	public float getAlfa() {
		return alfa;
	}

	public void setAlfa(float alfa) {
		this.alfa = alfa;
	}

	public int getSAMax() {
		return SAMax;
	}

	public void setSAMax(int sAMax) {
		SAMax = sAMax;
	}

	public double getT0() {
		return t0;
	}

	public void setT0(double t0) {
		this.t0 = t0;
	}

	public double getLimiarMinimoTemperatura() {
		return limiarMinimoTemperatura;
	}

	public void setLimiarMinimoTemperatura(double limiarMinimoTemperatura) {
		this.limiarMinimoTemperatura = limiarMinimoTemperatura;
	}

	public Solucao getSolucaoCorrente() {
		return solucaoCorrente;
	}

	public void setSolucaoCorrente(Solucao solucaoCorrente) {
		this.solucaoCorrente = solucaoCorrente;
	}

	public StringBuilder getResultadosTemperaturas() {
		return resultadosTemperaturas;
	}

	public void setResultadosTemperaturas(StringBuilder resultadosTemperaturas) {
		this.resultadosTemperaturas = resultadosTemperaturas;
	}
	
}
