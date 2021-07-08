package br.com.alura.microservico.loja.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import br.com.alura.microservico.loja.dto.InfoFornecedoDTO;
import br.com.alura.microservico.loja.dto.InfoPedidoDTO;
import br.com.alura.microservico.loja.dto.ItemDaCompraDTO;


@FeignClient("fornecedor")
public interface FornecedorClient {
	
	@RequestMapping("/info/{estado}")
	InfoFornecedoDTO getInfoPorEstado(@PathVariable String estado);

	@RequestMapping(method = RequestMethod.POST,  value = "/pedido" )
	InfoPedidoDTO realizaPedido(List<ItemDaCompraDTO> itens);
	
}
