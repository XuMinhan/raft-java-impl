package net.RPC.entity;


import java.util.Objects;

public class NodeIdAndOqId {
    Integer nodeId;
    Integer OqId;

    public NodeIdAndOqId() {
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public Integer getOqId() {
        return OqId;
    }

    public void setOqId(Integer oqId) {
        OqId = oqId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;  // 引用相同
        if (o == null || getClass() != o.getClass()) return false;  // 对象为空或者类型不一致
        NodeIdAndOqId that = (NodeIdAndOqId) o;  // 强制类型转换
        return Objects.equals(nodeId, that.nodeId) &&
                Objects.equals(OqId, that.OqId);  // 判断两个字段是否相等
    }

    @Override
    public String toString() {
        return "NodeIdAndOqId{" +
                "nodeId=" + nodeId +
                ", OqId=" + OqId +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, OqId);  // 生成基于 nodeId 和 OqId 的哈希值
    }




    public NodeIdAndOqId(Integer nodeId, Integer oqId) {
        this.nodeId = nodeId;
        OqId = oqId;
    }
}
