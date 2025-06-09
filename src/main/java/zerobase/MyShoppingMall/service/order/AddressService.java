package zerobase.MyShoppingMall.service.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.MyShoppingMall.domain.Address;
import zerobase.MyShoppingMall.domain.Member;
import zerobase.MyShoppingMall.repository.address.AddressRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

//    public void createAddress(OrderAddress orderAddress, Member member) {
//        Address address = Address.builder()
//                .receiverName(orderAddress.getRecipientName())
//                .addr(orderAddress.getAddressLine1())
//                .addrDetail(orderAddress.getAddressLine2())
//                .receiverPhone(orderAddress.getRecipientPhone())
//                .member(member)
//                .build();
//
//        addressRepository.save(address);
//    }

    public List<Address> getAddressByMember(Member member) {
        return addressRepository.findByMember(member);
    }

    public void deleteAddress(Long addressId, Member member) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("주소를 찾을 수 없습니다."));

        if (!address.getMember().equals(member)) {
            throw new SecurityException("주소를 삭제할 수 없습니다.");
        }

        addressRepository.delete(address);
    }


    public Address getAddressByIdAndMember(Long id, Member member) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("주소를 찾을 수 없습니다."));

        if (!address.getMember().equals(member)) {
            throw new SecurityException("본인의 주소만 찾을 수 있습니다.");
        }

        addressRepository.save(address);
        return address;
    }

//    @Transactional
//    public void updateAddress(Long addressId, OrderAddress orderAddress, Member member) {
//        Address address = getAddressByIdAndMember(addressId, member);
//        address.setReceiverName(orderAddress.getRecipientName());
//        address.setAddr(orderAddress.getAddressLine1());
//        address.setAddrDetail(orderAddress.getAddressLine2());
//        address.setReceiverPhone(orderAddress.getRecipientPhone());
//        address.setPostalCode(orderAddress.getPostalCode());
//        addressRepository.save(address);
//    }
}
