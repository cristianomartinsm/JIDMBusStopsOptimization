package modelo;

public class Bairro {

	private int id_bairro;
	private String nome;
	private int num_bairro;
	private String nome_bairro_ibge;
	private int populacao_ibge_2010;
	private double comprimento_metros_rua;
	private int quant_pontos_onibus;
	private double metros_rua_por_ponto_onibus;
	
	private double quantPontosOnibusAtual;
	private double metrosRuaPorPontoAtual;
	
	private static double mediaMetrosRuaPorPontoOnibus;
	
	public Bairro() {
		
	}
	
	public Bairro(Bairro bairro) {
		setId_bairro(bairro.getId_bairro());
		setNome(bairro.getNome());
		setNum_bairro(bairro.getNum_bairro());
		setNome_bairro_ibge(bairro.getNome_bairro_ibge());
		setPopulacao_ibge_2010(bairro.getPopulacao_ibge_2010());
		setComprimento_metros_rua(bairro.getComprimento_metros_rua());
		setQuant_pontos_onibus(bairro.getQuant_pontos_onibus());
		setMetros_rua_por_ponto_onibus(bairro.getMetros_rua_por_ponto_onibus());
		setQuantPontosOnibusAtual(bairro.getQuantPontosOnibusAtual());
		setMetrosRuaPorPontoAtual(bairro.getMetrosRuaPorPontoAtual());
	}
	
	public int getId_bairro() {
		return id_bairro;
	}
	
	public void setId_bairro(int id_bairro) {
		this.id_bairro = id_bairro;
	}
	
	public String getNome() {
		return nome;
	}
	
	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public int getNum_bairro() {
		return num_bairro;
	}
	
	public void setNum_bairro(int num_bairro) {
		this.num_bairro = num_bairro;
	}
	
	public String getNome_bairro_ibge() {
		return nome_bairro_ibge;
	}
	
	public void setNome_bairro_ibge(String nome_bairro_ibge) {
		this.nome_bairro_ibge = nome_bairro_ibge;
	}
	
	public int getPopulacao_ibge_2010() {
		return populacao_ibge_2010;
	}

	public void setPopulacao_ibge_2010(int populacao_ibge_2010) {
		this.populacao_ibge_2010 = populacao_ibge_2010;
	}

	public double getComprimento_metros_rua() {
		return comprimento_metros_rua;
	}

	public void setComprimento_metros_rua(double comprimento_metros_rua) {
		this.comprimento_metros_rua = comprimento_metros_rua;
	}

	public int getQuant_pontos_onibus() {
		return quant_pontos_onibus;
	}

	public void setQuant_pontos_onibus(int quant_pontos_onibus) {
		this.quant_pontos_onibus = quant_pontos_onibus;
	}

	public double getMetros_rua_por_ponto_onibus() {
		return metros_rua_por_ponto_onibus;
	}

	public void setMetros_rua_por_ponto_onibus(double metros_rua_por_ponto_onibus) {
		this.metros_rua_por_ponto_onibus = metros_rua_por_ponto_onibus;
	}

	public double getMetrosRuaPorPontoAtual() {
		return metrosRuaPorPontoAtual;
	}

	public void setMetrosRuaPorPontoAtual(double metrosRuaPorPontoAtual) {
		this.metrosRuaPorPontoAtual = metrosRuaPorPontoAtual;
	}

	public double getQuantPontosOnibusAtual() {
		return quantPontosOnibusAtual;
	}

	public void setQuantPontosOnibusAtual(double quantPontosOnibusAtual) {
		this.quantPontosOnibusAtual = quantPontosOnibusAtual;
	}

	public static double getMediaMetrosRuaPorPontoOnibus() {
		return mediaMetrosRuaPorPontoOnibus;
	}

	public static void setMediaMetrosRuaPorPontoOnibus(double mediaMetrosRuaPorPontoOnibus) {
		Bairro.mediaMetrosRuaPorPontoOnibus = mediaMetrosRuaPorPontoOnibus;
	}
	
}
