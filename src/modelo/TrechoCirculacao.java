package modelo;

import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.plaf.basic.BasicTreeUI.TreeCancelEditingAction;

import org.jgrapht.graph.DefaultWeightedEdge;

public class TrechoCirculacao extends DefaultWeightedEdge {
	
	private int id_tr_circ; //ID do trecho
	private int id_tp_trci; //ID do tipo do trecho
							/*	1: "Trecho de via."
								2: "Trecho de conversão."
								3: "Trecho de pista exclusiva de ônibus."
								4: "Trecho de estação de ônibus."
								5: "Trecho de rotatória."	*/
	private int id_lograd; //ID do logradouro do trecho
	private NoCirculacao noInicial; //Nó do início do trecho
	private NoCirculacao noFinal; //Nó do final do trecho
	
	private ArrayList<PontoOnibus> pontosOnibus;

	private double distancia;
	
	private boolean caminhado;

	private ArrayList<Bairro> bairros;
	
	public TrechoCirculacao () {
		setPontosOnibus(new ArrayList<PontoOnibus>());
		setCaminhado(false);
		setBairros(new ArrayList<Bairro>());
	}

	public TrechoCirculacao (TrechoCirculacao trechoCirculacao) {
		setId_tr_circ(trechoCirculacao.getId_tr_circ());
		setId_tp_trci(trechoCirculacao.getId_tp_trci());
		setId_lograd(trechoCirculacao.getId_lograd());
		setCaminhado(trechoCirculacao.isCaminhado());
		setNoInicial(new NoCirculacao(trechoCirculacao.getNoInicial().getIdString(), trechoCirculacao.getNoInicial().isCaminhado()));
		setNoFinal(new NoCirculacao(trechoCirculacao.getNoFinal().getIdString(), trechoCirculacao.getNoFinal().isCaminhado()));
		
		setBairros(new ArrayList<Bairro>());
		for (Bairro bairro : trechoCirculacao.getBairros()) {
			getBairros().add(new Bairro(bairro));
		}
		
		setPontosOnibus(new ArrayList<PontoOnibus>());
		for (PontoOnibus pontoOnibus : trechoCirculacao.getPontosOnibus()) {
			PontoOnibus novoPontoOnibus = null;
			if (pontoOnibus instanceof PontoOnibusNovo) {
				//O HashSet de TrechosCaminhados é reconstruído na classe RedeCirculacaoViaria com base nos objetos novos
				novoPontoOnibus = new PontoOnibusNovo(this, ((PontoOnibusNovo) pontoOnibus).getIdPontoNovo(), ((PontoOnibusNovo) pontoOnibus).getTrechosCaminhados());
				((PontoOnibusNovo) novoPontoOnibus).setBeneficioPonto(((PontoOnibusNovo) pontoOnibus).getBeneficioPonto());
			}
			else {
				novoPontoOnibus = new PontoOnibus();
			}
			novoPontoOnibus.setIdPontoOnibus(pontoOnibus.getIdPontoOnibus());
			novoPontoOnibus.setLogradouro(pontoOnibus.getLogradouro());
			novoPontoOnibus.setEndereco(pontoOnibus.getEndereco());
			novoPontoOnibus.setTrechoCirculacao(this);
			novoPontoOnibus.setGeomPonto(pontoOnibus.getGeomPonto());
			
			getPontosOnibus().add(novoPontoOnibus);				
		}
		
		setDistancia(trechoCirculacao.getDistancia());
	}
	
	public int getId_tr_circ() {
		return id_tr_circ;
	}
	
	public void setId_tr_circ(int id_tr_circ) {
		this.id_tr_circ = id_tr_circ;
	}
	
	public int getId_tp_trci() {
		return id_tp_trci;
	}
	
	public void setId_tp_trci(int id_tp_trci) {
		this.id_tp_trci = id_tp_trci;
	}
	
	public int getId_lograd() {
		return id_lograd;
	}
	
	public void setId_lograd(int id_lograd) {
		this.id_lograd = id_lograd;
	}
	
	public NoCirculacao getNoInicial() {
		return noInicial;
	}

	public void setNoInicial(NoCirculacao noInicial) {
		this.noInicial = noInicial;
	}

	public NoCirculacao getNoFinal() {
		return noFinal;
	}

	public void setNoFinal(NoCirculacao noFinal) {
		this.noFinal = noFinal;
	}

	public double getDistancia() {
		return distancia;
	}

	public void setDistancia(double distancia) {
		this.distancia = distancia;
	}
	
	@Override
	protected Object getSource() {
		return this.getNoInicial();
	}
	
	@Override
	protected Object getTarget() {
		return this.getNoFinal();
	}
	
	@Override
	protected double getWeight() {
		return this.getDistancia();
	}

	public ArrayList<PontoOnibus> getPontosOnibus() {
		return pontosOnibus;
	}

	public void setPontosOnibus(ArrayList<PontoOnibus> pontosOnibus) {
		this.pontosOnibus = pontosOnibus;
	}

	public boolean isCaminhado() {
		return caminhado;
	}

	public void setCaminhado(boolean caminhado) {
		this.caminhado = caminhado;
	}

	public ArrayList<Bairro> getBairros() {
		return bairros;
	}

	public void setBairros(ArrayList<Bairro> bairros) {
		this.bairros = bairros;
	}

}
