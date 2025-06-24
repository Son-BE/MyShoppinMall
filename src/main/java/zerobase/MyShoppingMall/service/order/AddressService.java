package zerobase.MyShoppingMall.service.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.MyShoppingMall.dto.order.AddressSaveRequest;
import zerobase.MyShoppingMall.entity.Address;
import zerobase.MyShoppingMall.entity.Member;
import zerobase.MyShoppingMall.repository.address.AddressRepository;
import zerobase.MyShoppingMall.repository.member.MemberRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final MemberRepository memberRepository;

    public void deleteAddress(Long addressId, Member member) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("주소를 찾을 수 없습니다."));

        if (!address.getMember().equals(member)) {
            throw new SecurityException("주소를 삭제할 수 없습니다.");
        }

        addressRepository.delete(address);
    }

    public Address  findDefaultAddressByMemberId(Long memberId) {
        return addressRepository.findByMemberIdAndIsDefaultTrue(memberId)
                .orElseThrow(() -> new IllegalArgumentException(("디폴트 주소 없음")));
    }

    @Transactional
    public void saveDefaultAddress(AddressSaveRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        Address address = new Address();
        address.setReceiverName(request.getReceiverName());
        address.setAddr(request.getAddr());
        address.setAddrDetail(request.getAddrDetail());
        address.setPostalCode(request.getPostalCode());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setDefaultAddress(true);
        address.setMember(member);

        addressRepository.save(address);

        List<Address> addresses = addressRepository.findAllByMemberId(memberId);
        for (Address a : addresses) {
            if (!a.equals(address) && a.isDefaultAddress()) {
                a.setDefaultAddress(false);
            }
        }
    }

    @Transactional
    public void setDefaultAddress(Member member, Long newDefaultAddressId) {
        // 기존 기본 배송지 false 처리
        addressRepository.findByMemberAndIsDefaultTrue(member)
                .ifPresent(addr -> addr.setDefaultAddress(false));

        // 새로운 기본 배송지 true 처리
        Address newDefault = addressRepository.findById(newDefaultAddressId)
                .orElseThrow(() -> new IllegalArgumentException("주소가 존재하지 않습니다."));
        newDefault.setDefaultAddress(true);
    }


}
