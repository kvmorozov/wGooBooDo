import React from "react";
import classNames from "classnames";
import {Label} from "semantic-ui-react";

const renderer = (node, treeOptions) => {
    const {id, name, loadOnDemand = false, children, state, props = {}, itemType, itemSubType, filesCount} = node;
    const droppable = (treeOptions.droppable) && (props.droppable);
    const {depth, open, path, total, loading = false, selected = false} = state;
    const childrenLength = Object.keys(children).length;
    const more = node.hasChildren();

    var itemStyle = {
        fontSize: "200%"
    };

    const isDir = itemType == "storage";
    const isPdf = !isDir && (itemSubType == 'pdf');

    return (
        <div
            className={classNames(
                'infinite-tree-item',
                {'infinite-tree-selected': selected}
            )}
            data-id={id}
            data-expanded={more && open}
            data-depth={depth}
            data-path={path}
            data-selected={selected}
            data-children={childrenLength}
            data-total={total}>
            <div
                className="infinite-tree-node"
                style={{marginLeft: depth * 18}}>
                {!more && loadOnDemand &&
                <a className={classNames(treeOptions.togglerClass, 'infinite-tree-closed')}>
                    <i className="glyphicon glyphicon-triangle-right"/>
                </a>
                }
                {more && open &&
                <a className={classNames(treeOptions.togglerClass)}>
                    <i className="glyphicon glyphicon-triangle-bottom"/>
                </a>
                }
                {more && !open &&
                <a className={classNames(treeOptions.togglerClass, 'infinite-tree-closed')}>
                    <i className="glyphicon glyphicon-triangle-right"/>
                </a>
                }
                <Label image className="infinite-tree-title">
                    <img src={selected ? '/icons/folder-open-icon.png' : '/icons/folder-closed-icon.png'}/>
                    {name}
                </Label>
                <i
                    style={{marginLeft: 5}}
                    className={classNames(
                        {'hidden': !loading},
                        'glyphicon',
                        'glyphicon-refresh',
                        {'rotating': loading}
                    )}
                />
            </div>
        </div>
    );
};

export default renderer;
