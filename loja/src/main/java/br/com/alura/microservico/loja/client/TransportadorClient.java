package br.com.alura.microservico.loja.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import br.com.alura.microservico.loja.dto.VoucherDTO;
import br.com.alura.microservico.loja.dto.infoEntregaDTO;



@FeignClient("transportador")
public interface TransportadorClient {

	@RequestMapping(path = "/entrega", method = RequestMethod.POST)
	public VoucherDTO reservaEntrega(infoEntregaDTO pedidoDTO);
}
