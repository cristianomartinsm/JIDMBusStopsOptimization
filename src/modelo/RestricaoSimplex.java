package modelo;

import java.util.Set;

public class RestricaoSimplex {

	private Integer id;
	private NoCirculacao noGerador;
	private Set<TrechoCirculacao> trechosProximos;
	private Integer maxPontosPermitidos;
	private Integer igualdadeInequaldade;
	
	private static Integer IGUALDADE = 0;
	private static Integer INEQUALDADE = 1;
	
	private Integer metrosLimite;
	
	public RestricaoSimplex(Integer id, NoCirculacao noGerador, Set<TrechoCirculacao> trechosProximos, Integer maxPontosPermitidos, Integer igualdadeInequaldade, Integer metrosLimite) {
		this.id = id;
		this.noGerador = noGerador;
		this.trechosProximos = trechosProximos;
		this.maxPontosPermitidos = maxPontosPermitidos;
		this.igualdadeInequaldade = igualdadeInequaldade;
		this.metrosLimite = metrosLimite;
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public NoCirculacao getNoGerador() {
		return noGerador;
	}

	public void setNoGerador(NoCirculacao noGerador) {
		this.noGerador = noGerador;
	}

	public Set<TrechoCirculacao> getTrechosProximos() {
		return trechosProximos;
	}
	
	public void setTrechosProximos(Set<TrechoCirculacao> trechosProximos) {
		this.trechosProximos = trechosProximos;
	}
	
	public Integer getMaxPontosPermitidos() {
		return maxPontosPermitidos;
	}
	
	public void setMaxPontosPermitidos(Integer maxPontosPermitidos) {
		this.maxPontosPermitidos = maxPontosPermitidos;
	}

	public Integer getIgualdadeInequaldade() {
		return igualdadeInequaldade;
	}

	public void setIgualdadeInequaldade(Integer igualdadeInequaldade) {
		this.igualdadeInequaldade = igualdadeInequaldade;
	}

	public static Integer getIGUALDADE() {
		return IGUALDADE;
	}

	public static Integer getINEQUALDADE() {
		return INEQUALDADE;
	}

	public Integer getMetrosLimite() {
		return metrosLimite;
	}

	public void setMetrosLimite(Integer metrosLimite) {
		this.metrosLimite = metrosLimite;
	}
	
}
