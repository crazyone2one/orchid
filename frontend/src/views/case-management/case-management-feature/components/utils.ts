import {ModuleTreeNode} from "/@/models/common.ts";
import {findNodePathByKey} from "/@/utils";

export const getModules = (moduleIds: string, treeData: ModuleTreeNode[]) => {
    const modules = findNodePathByKey(treeData, moduleIds, undefined, 'id');
    if (modules) {
        const moduleName = (modules || [])?.treePath.map((item: any) => item.name);
        if (moduleName.length === 1) {
            return moduleName[0];
        }
        return `/${moduleName.join('/')}`;
    }
}