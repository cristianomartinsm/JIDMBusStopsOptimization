package modelo;

public class PontoOnibus {

	private long idPontoOnibus;
	private String logradouro;
	private String endereco;
	private TrechoCirculacao trechoCirculacao;
	private String geomPonto;
	
	public PontoOnibus() {
		
	}
	
	public PontoOnibus(long idPontoOnibus, String logradouro, String endereco, TrechoCirculacao trechoCirculacao, String geomPonto) {
		this.idPontoOnibus = idPontoOnibus;
		this.logradouro = logradouro;
		this.endereco = endereco;
		this.trechoCirculacao = trechoCirculacao;
		this.geomPonto = geomPonto;
	}

	public long getIdPontoOnibus() {
		return idPontoOnibus;
	}
	
	public void setIdPontoOnibus(long idPontoOnibus) {
		this.idPontoOnibus = idPontoOnibus;
	}
	
	public String getLogradouro() {
		return logradouro;
	}
	
	public void setLogradouro(String logradouro) {
		this.logradouro = logradouro;
	}
	
	public String getEndereco() {
		return endereco;
	}
	
	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}
	
	public TrechoCirculacao getTrechoCirculacao() {
		return trechoCirculacao;
	}

	public void setTrechoCirculacao(TrechoCirculacao trechoCirculacao) {
		this.trechoCirculacao = trechoCirculacao;
	}
	
	public String getGeomPonto() {
		return geomPonto;
	}
	
	public void setGeomPonto(String geomPonto) {
		this.geomPonto = geomPonto;
	}
	
}
