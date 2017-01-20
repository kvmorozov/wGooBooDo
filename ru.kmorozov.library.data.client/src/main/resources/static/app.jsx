define(function (require) {
    'use strict';

    var React = require('react');
    var client = require('./client');

    var App = React.createClass({
        getInitialState: function () {
            return ({storages: []});
        },
        componentDidMount: function () {
            client({
                method: 'GET', path: '/storagesByParentId?storageId='
            }).done(
                response => {
                    this.setState({storages: response.entity});
                }
            );
        },
        render: function () {
            return (
                <StorageList storages={this.state.storages}/>
            )
        }
    })

    var StorageList = React.createClass({
        render: function () {
            var storages = this.props.storages.map(storage =>
                <Storage key={storage.id} storage={storage}/>
            );
            return (
                <table>
                    <tr>
                        <th>Id</th>
                        <th>Storage type</th>
                        <th>Url</th>
                        <th>Parent</th>
                    </tr>
                    {storages}
                </table>
            )
        }
    })

    var Storage = React.createClass({
        render: function () {
            return (
                <tr>
                    <td>{this.props.storage.id}</td>
                    <td>{this.props.storage.storageType}</td>
                    <td>{this.props.storage.url}</td>
                    <td>{this.props.storage.parentId}</td>
                </tr>
            )
        }
    })

    React.render(
        <App />,
        document.getElementById('react')
    )
});