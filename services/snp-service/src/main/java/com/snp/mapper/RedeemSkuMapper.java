package com.snp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.snp.entity.RedeemSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RedeemSkuMapper extends BaseMapper<RedeemSku> {

    @Update("""
            UPDATE redeem_sku SET stock = stock - #{quantity}
            WHERE id = #{id} AND stock >= #{quantity}
            """)
    int decrementStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Update("""
            UPDATE redeem_sku SET stock = stock + #{quantity}
            WHERE id = #{id}
            """)
    int incrementStock(@Param("id") Long id, @Param("quantity") int quantity);
}
