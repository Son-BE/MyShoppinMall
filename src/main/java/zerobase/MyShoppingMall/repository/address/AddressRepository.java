package zerobase.MyShoppingMall.repository.address;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.MyShoppingMall.entity.Address;
import zerobase.MyShoppingMall.entity.Member;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByMember(Member member);

    Optional<Address> findByMemberIdAndIsDefaultTrue(Long memberId);

    List<Address> findAllByMemberId(Long memberId);

    Optional<Address> findByMemberAndIsDefaultTrue(Member member);
}
