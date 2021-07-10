package br.com.alura.microservico.loja.service;

import java.time.LocalDate;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import br.com.alura.microservico.loja.client.FornecedorClient;
import br.com.alura.microservico.loja.client.TransportadorClient;
import br.com.alura.microservico.loja.dto.CompraDTO;
import br.com.alura.microservico.loja.dto.InfoFornecedoDTO;
import br.com.alura.microservico.loja.dto.InfoPedidoDTO;
import br.com.alura.microservico.loja.dto.VoucherDTO;
import br.com.alura.microservico.loja.dto.infoEntregaDTO;
import br.com.alura.microservico.loja.model.Compra;
import br.com.alura.microservico.loja.model.compraState;
import br.com.alura.microservico.loja.repository.CompraRepository;

@Service
public class CompraService {

	private static final Logger LOG = LoggerFactory.getLogger(CompraService.class);
	
	@Autowired
	private FornecedorClient fornecedorClient;
	
	@Autowired
	private CompraRepository compraRepository;
	
	@Autowired
	private TransportadorClient transportadorClient;
	
	@HystrixCommand(threadPoolKey = "getByIdThreadPool")
	public Compra getById(Long id) {
		Optional<Compra> optionalCompra = compraRepository.findById(id);
		return optionalCompra.get();
	}
	
	public Compra reprocessaCompra(Long id) {
		return null;
	}
	
	public Compra cancelaCompra(Long id) {
		return null;
	}
	
	@HystrixCommand(fallbackMethod = "realizaCompraFallback", threadPoolKey = "realizaCompraThreadPool" )
	public Compra realizaCompra(CompraDTO compra) {
		
		Compra compraSalva = new Compra();
		compraSalva.setState(compraState.RECEBIDO);
		compraSalva.setEnderecoDestino(compra.getEndereco().toString());
		compraRepository.save(compraSalva);
		compra.setCompraId(compraSalva.getId());
		
		final String estado = compra.getEndereco().getEstado();
		//Comunicação com microservico de fornecedir para pegar o estado
		InfoFornecedoDTO info = fornecedorClient.getInfoPorEstado(estado);
		
		//Comunicação com microservico de fornecedor para informações de pedido
		InfoPedidoDTO  pedido = fornecedorClient.realizaPedido(compra.getItens());
		compraSalva.setState(compraState.PEDIDO_REALIZADO);
		compraSalva.setPedidoId(pedido.getId());
		compraSalva.setTempoDePreparo(pedido.getTempoDePreparo());
		compraRepository.save(compraSalva);
		
		//SIMULAR QUANDO DÁ UM ERRO E ELE ARMAZENA EM QUAL ESTADO ELE PAROU
		//if(1==1) throw new RuntimeException();
		
		infoEntregaDTO entregaDTO = new infoEntregaDTO();
		entregaDTO.setPedidoId(pedido.getId());
		entregaDTO.setDataParaEntrega(LocalDate.now().plusDays(pedido.getTempoDePreparo()));
		entregaDTO.setEnderecoOrigem(info.getEndereco());
		entregaDTO.setEnderecoDestino(compra.getEndereco().toString());
		
		//COMUNICAÇÃO COM O MICROSERVICO DE TRANSPORTADOR
		VoucherDTO voucher = transportadorClient.reservaEntrega(entregaDTO);
		compraSalva.setState(compraState.RESERVA_ENTREGA_REALIZADA);
		compraSalva.setDataParaEntrega(voucher.getPrevisaoParaEntrega());
		compraSalva.setVoucher(voucher.getNumero());
		compraRepository.save(compraSalva);
				
		return compraSalva;
	}
	
	public Compra realizaCompraFallback(CompraDTO compra) {
		
		if(compra.getCompraId() != null) {
			return compraRepository.findById(compra.getCompraId()).get();
		}
		
		Compra compraFallback = new Compra();
		compraFallback.setEnderecoDestino(compra.getEndereco().toString());
		
		return compraFallback;
	}



}
